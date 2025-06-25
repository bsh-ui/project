import React, { useState, useEffect, useRef } from 'react';

// API 기본 URL 설정
const API_BASE_URL = 'http://localhost:8485/api/music';

// JWT 토큰을 쿠키에서 가져오는 헬퍼 함수
const getCookie = (name) => {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for(let i=0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
};

// duration (초)를 "분:초" 형식으로 변환하는 헬퍼 함수
const formatDuration = (seconds) => {
    if (seconds === null || isNaN(seconds) || seconds < 0) {
        return '00:00';
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60);
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
};

function MusicAdminPage() {
    const [musicList, setMusicList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [jwtToken, setJwtToken] = useState('');
    const [message, setMessage] = useState(''); // 성공/실패 메시지
    const [isSuccessMessage, setIsSuccessMessage] = useState(false); // 메시지 타입
    const messageTimeoutRef = useRef(null); // 메시지 타임아웃 ID 저장

    // 업로드 폼 상태
    const [uploadFormData, setUploadFormData] = useState({
        title: '',
        artist: '',
        album: '',
        lyrics: '',
        musicFile: null,
        coverImageFile: null
    });

    // 수정 모드 관련 상태
    const [editingMusicId, setEditingMusicId] = useState(null);
    const [editedMusicData, setEditedMusicData] = useState({}); // 현재 수정 중인 음악의 데이터
    const allMusicDataRef = useRef({}); // 모든 음악 데이터를 저장 (원본 HTML의 allMusicData 역할)

    // 가사 섹션 토글 상태 (각 음악 ID별로 관리)
    const [expandedLyrics, setExpandedLyrics] = useState({});

    // 확인 모달 상태
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [confirmAction, setConfirmAction] = useState(null); // 모달에서 실행할 함수
    const [confirmMessage, setConfirmMessage] = useState(''); // 확인 모달 메시지

    // 페이지네이션 (현재는 사용하지 않지만, 추후 확장성을 위해 변수로 유지)
    const currentPage = useRef(0);
    const pageSize = 10;

    // 메시지 표시 및 자동 숨김 함수
    const displayMessage = (msg, success) => {
        // 이전 타임아웃이 있다면 클리어
        if (messageTimeoutRef.current) {
            clearTimeout(messageTimeoutRef.current);
        }
        setMessage(msg);
        setIsSuccessMessage(success);
        // 3초 후 메시지 숨김
        messageTimeoutRef.current = setTimeout(() => {
            setMessage('');
        }, 3000);
    };

    // 초기 로드 시 JWT 토큰 가져오기 및 음악 목록 불러오기
    useEffect(() => {
        const storedToken = getCookie('jwt_token');
        if (storedToken) {
            setJwtToken(storedToken);
            console.log('[MusicAdminPage] JWT 토큰을 쿠키에서 성공적으로 가져왔습니다.');
            fetchMusicList(storedToken); // 토큰이 있을 때만 목록 로드 시도
        } else {
            console.warn('[MusicAdminPage] JWT 토큰이 쿠키에 없습니다. 관리자 기능 사용에 제한이 있을 수 있습니다.');
            displayMessage('로그인이 필요하거나 관리자 권한이 없습니다. 로그인 페이지로 이동합니다.', false);
            setLoading(false);
            setTimeout(() => { window.location.href = '/custom_login'; }, 2000); // 로그인 페이지로 리디렉션
        }

        // 컴포넌트 언마운트 시 타임아웃 클리어
        return () => {
            if (messageTimeoutRef.current) {
                clearTimeout(messageTimeoutRef.current);
            }
        };
    }, []);

    // 음악 목록 불러오기 및 UI 갱신 함수
    const fetchMusicList = async (token) => {
        setLoading(true);
        allMusicDataRef.current = {}; // 목록 새로 로드 전에 데이터 초기화
        setExpandedLyrics({}); // 가사 토글 상태도 초기화

        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}?page=${currentPage.current}&size=${pageSize}&sort=uploadDate,desc`, { headers: headers });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    displayMessage('음악 목록을 볼 권한이 없습니다. 관리자 계정으로 로그인해주세요.', false);
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000); // 로그인 페이지로 리디렉션
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const pageData = await response.json();
            const musicData = pageData.content;

            const newMusicDataMap = {};
            musicData.forEach(music => {
                newMusicDataMap[music.id] = music;
            });
            allMusicDataRef.current = newMusicDataMap; // 모든 음악 데이터를 참조에 저장
            setMusicList(musicData);

        } catch (error) {
            console.error('음악 목록을 불러오는 데 실패했습니다:', error);
            displayMessage('음악 목록을 불러오는 데 실패했습니다.', false);
            setMusicList([]); // 오류 발생 시 목록 비우기
        } finally {
            setLoading(false);
        }
    };

    // 업로드 폼 입력 변경 핸들러
    const handleUploadChange = (e) => {
        const { id, value, files } = e.target;
        setUploadFormData(prev => ({
            ...prev,
            [id]: files ? files[0] : value
        }));
    };

    // 음악 업로드 폼 제출 처리
    const handleUploadSubmit = async (e) => {
        e.preventDefault();

        if (!uploadFormData.musicFile) {
            displayMessage('음악 파일을 선택해주세요.', false);
            return;
        }
        if (!uploadFormData.title.trim() || !uploadFormData.artist.trim()) {
            displayMessage('제목과 아티스트는 필수 입력 항목입니다.', false);
            return;
        }

        const formData = new FormData();
        formData.append('title', uploadFormData.title);
        formData.append('artist', uploadFormData.artist);
        formData.append('album', uploadFormData.album || ''); // null 대신 빈 문자열
        formData.append('lyricsContent', uploadFormData.lyrics || ''); // null 대신 빈 문자열
        formData.append('file', uploadFormData.musicFile);
        if (uploadFormData.coverImageFile) {
            formData.append('coverImageFile', uploadFormData.coverImageFile);
        }

        const headers = {};
        if (jwtToken) {
            headers['Authorization'] = `Bearer ${jwtToken}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/upload`, {
                method: 'POST',
                body: formData,
                headers: headers
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    displayMessage('음악을 업로드할 권한이 없습니다. 관리자 계정으로 로그인해주세요.', false);
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                    return;
                }
                const errorText = await response.text();
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
            }

            const result = await response.json();
            displayMessage(`음악이 성공적으로 업로드되었습니다: ${result.title}`, true);
            resetUploadForm();
            fetchMusicList(jwtToken); // 업로드 후 목록 새로고침
        } catch (error) {
            console.error('음악 업로드 실패:', error);
            displayMessage(`음악 업로드에 실패했습니다: ${error.message}`, false);
        }
    };

    // 폼 초기화 함수
    const resetUploadForm = () => {
        setUploadFormData({
            title: '',
            artist: '',
            album: '',
            lyrics: '',
            musicFile: null,
            coverImageFile: null
        });
        // 파일 input 강제 초기화
        const musicFileInput = document.getElementById('musicFile');
        if (musicFileInput) musicFileInput.value = '';
        const coverImageFileInput = document.getElementById('coverImageFile');
        if (coverImageFileInput) coverImageFileInput.value = '';
    };

    // 재생 버튼 클릭 시
    const playMusic = (musicId, title) => {
        const audioPlayer = document.querySelector(`#audio-${musicId}`);
        if (audioPlayer) {
            audioPlayer.play();
            displayMessage(`${title} 재생 시작`, true);
            // TODO: 재생 횟수 증가 API 호출 (백엔드 구현 후)
        } else {
            displayMessage('플레이어를 찾을 수 없습니다.', false);
        }
    };

    // 삭제 함수 (확인 모달 트리거)
    const confirmDeleteMusic = (musicId, title) => {
        setConfirmMessage(`정말로 "${title}" 음악을 삭제하시겠습니까?`);
        setConfirmAction(() => () => handleDeleteMusic(musicId)); // 클로저로 musicId 전달
        setShowConfirmModal(true);
    };

    // 실제 삭제 로직
    const handleDeleteMusic = async (musicId) => {
        setShowConfirmModal(false); // 확인 모달 닫기

        const headers = {};
        if (jwtToken) {
            headers['Authorization'] = `Bearer ${jwtToken}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/${musicId}`, {
                method: 'DELETE',
                headers: headers
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    displayMessage('음악을 삭제할 권한이 없습니다. 관리자 계정으로 로그인해주세요.', false);
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                    return;
                }
                const errorText = await response.text();
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
            }

            displayMessage(`음악이 성공적으로 삭제되었습니다.`, true);
            fetchMusicList(jwtToken); // 삭제 후 목록 새로고침
        } catch (error) {
            console.error('음악 삭제 실패:', error);
            displayMessage(`음악 삭제에 실패했습니다: ${error.message}`, false);
        }
    };

    // 수정 모드 진입
    const editMusic = (musicId) => {
        setEditingMusicId(musicId);
        const currentMusic = allMusicDataRef.current[musicId];
        if (currentMusic) {
            setEditedMusicData({
                ...currentMusic,
                artist: Array.isArray(currentMusic.artist) ? currentMusic.artist.join(', ') : currentMusic.artist,
                album: currentMusic.album || '',
                lyrics: currentMusic.lyrics || ''
            });
        } else {
            displayMessage('수정할 음악 데이터를 찾을 수 없습니다.', false);
        }
    };

    // 수정 중인 음악 데이터 변경 핸들러
    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setEditedMusicData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // 수정 내용 저장 함수
    const saveEditedMusic = async (musicId) => {
        const newTitle = editedMusicData.title.trim();
        const newArtistString = editedMusicData.artist.trim();
        const newAlbum = editedMusicData.album.trim();
        const newLyrics = editedMusicData.lyrics.trim();

        if (!newTitle || !newArtistString) {
            displayMessage('제목과 아티스트는 필수 입력 항목입니다.', false);
            return;
        }

        const newArtistArray = newArtistString.split(',').map(item => item.trim()).filter(item => item !== '');

        const updatedMusic = {
            ...allMusicDataRef.current[musicId], // 기존 데이터 유지
            title: newTitle,
            artist: newArtistArray,
            album: newAlbum,
            lyrics: newLyrics
        };

        const headers = {
            'Content-Type': 'application/json'
        };
        if (jwtToken) {
            headers['Authorization'] = `Bearer ${jwtToken}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/${musicId}`, {
                method: 'PUT',
                headers: headers,
                body: JSON.stringify(updatedMusic)
            });

            if (!response.ok) {
                let errorData = await response.text();
                try {
                    errorData = JSON.parse(errorData);
                } catch (e) {
                    // Not JSON, just use as is
                }
                console.error('서버 응답 오류 데이터:', errorData);

                if (response.status === 401 || response.status === 403) {
                    displayMessage('음악을 수정할 권한이 없습니다. 관리자 계정으로 로그인해주세요.', false);
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorData.message || response.statusText || errorData}`);
            }

            displayMessage('음악 정보가 성공적으로 수정되었습니다.', true);
            setEditingMusicId(null); // 수정 모드 종료
            setEditedMusicData({}); // 수정 데이터 초기화
            fetchMusicList(jwtToken); // 목록 새로고침
        } catch (error) {
            console.error('음악 정보 수정 실패:', error);
            displayMessage(`음악 정보 수정에 실패했습니다: ${error.message}`, false);
        }
    };

    // 수정 취소 함수
    const cancelEdit = () => {
        setEditingMusicId(null);
        setEditedMusicData({});
        displayMessage('음악 수정이 취소되었습니다.', false);
    };

    // 가사 토글 핸들러
    const toggleLyrics = (musicId) => {
        setExpandedLyrics(prev => ({
            ...prev,
            [musicId]: !prev[musicId]
        }));
    };

    return (
        <div className="font-sans bg-gray-100 text-gray-800 min-h-screen pb-10">
            {/* Message Display */}
            {message && (
                <div className={`fixed top-4 right-4 z-50 p-4 rounded-md shadow-lg ${isSuccessMessage ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {message}
                </div>
            )}

            <div className="container max-w-5xl mx-auto mt-12 mb-12 px-4">
                <div className="main-nav text-right mb-5">
                    <a href="/main" className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded-md transition duration-150 ease-in-out">
                        메인으로
                    </a>
                </div>
                <h2 className="text-3xl font-bold mb-8 text-center text-gray-900">음악 관리 (업로드 & 재생)</h2>

                {/* 새로운 음악 업로드 섹션 */}
                <div className="card bg-white p-6 rounded-lg shadow-xl mb-8">
                    <div className="card-header bg-blue-600 text-white text-2xl font-bold py-4 px-6 rounded-t-lg -mx-6 -mt-6 mb-6">
                        새로운 음악 업로드
                    </div>
                    <form onSubmit={handleUploadSubmit} className="space-y-5">
                        <div>
                            <label htmlFor="title" className="block text-gray-700 font-semibold mb-1">제목</label>
                            <input type="text" id="title" name="title" className="w-full p-3 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                value={uploadFormData.title} onChange={handleUploadChange} required />
                        </div>
                        <div>
                            <label htmlFor="artist" className="block text-gray-700 font-semibold mb-1">아티스트</label>
                            <input type="text" id="artist" name="artist" className="w-full p-3 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                value={uploadFormData.artist} onChange={handleUploadChange} required />
                        </div>
                        <div>
                            <label htmlFor="album" className="block text-gray-700 font-semibold mb-1">앨범 (선택 사항)</label>
                            <input type="text" id="album" name="album" className="w-full p-3 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                value={uploadFormData.album} onChange={handleUploadChange} />
                        </div>
                        <div>
                            <label htmlFor="lyrics" className="block text-gray-700 font-semibold mb-1">가사 (선택 사항)</label>
                            <textarea id="lyrics" name="lyrics" rows="5" placeholder="가사를 입력하세요." className="w-full p-3 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                value={uploadFormData.lyrics} onChange={handleUploadChange}></textarea>
                        </div>
                        <div>
                            <label htmlFor="musicFile" className="block text-gray-700 font-semibold mb-1">음악 파일 (.mp3, .wav 등)</label>
                            <input type="file" id="musicFile" name="musicFile" accept="audio/*" className="w-full p-3 border border-gray-300 rounded-md file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                                onChange={handleUploadChange} required />
                        </div>
                        <div>
                            <label htmlFor="coverImageFile" className="block text-gray-700 font-semibold mb-1">커버 이미지 (선택 사항)</label>
                            <input type="file" id="coverImageFile" name="coverImageFile" accept="image/*" className="w-full p-3 border border-gray-300 rounded-md file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-green-50 file:text-green-700 hover:file:bg-green-100"
                                onChange={handleUploadChange} />
                        </div>
                        <div className="flex space-x-3">
                            <button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-md transition duration-150 ease-in-out">
                                업로드
                            </button>
                            <button type="button" className="flex-1 bg-gray-500 hover:bg-gray-600 text-white font-bold py-3 px-6 rounded-md transition duration-150 ease-in-out"
                                onClick={resetUploadForm}>
                                초기화
                            </button>
                        </div>
                    </form>
                </div>

                {/* 업로드된 음악 목록 섹션 */}
                <div className="card bg-white p-6 rounded-lg shadow-xl">
                    <div className="card-header bg-blue-600 text-white text-2xl font-bold py-4 px-6 rounded-t-lg -mx-6 -mt-6 mb-6">
                        업로드된 음악 목록
                    </div>
                    <div id="music-list-container">
                        {loading ? (
                            <p className="text-center text-gray-500 text-lg py-10">음악을 불러오는 중...</p>
                        ) : musicList.length === 0 ? (
                            <p className="text-center text-gray-500 text-lg py-10">등록된 음악이 없습니다. 첫 번째 음악을 업로드해주세요!</p>
                        ) : (
                            musicList.map(music => (
                                <div key={music.id} className={`music-item flex flex-col md:flex-row items-start md:items-center py-5 border-b border-dashed border-gray-200 last:border-b-0 ${editingMusicId === music.id ? 'bg-blue-50 p-4 rounded-lg -mx-4 my-2 shadow-inner' : ''}`}>
                                    <div className="music-info flex-grow mr-0 md:mr-4 min-w-[250px] w-full md:w-auto">
                                        {editingMusicId === music.id ? (
                                            <>
                                                <label className="block text-gray-700 font-semibold text-sm mb-1">제목</label>
                                                <input type="text" name="title" className="w-full p-2 border border-gray-300 rounded-md text-base mb-1"
                                                    value={editedMusicData.title} onChange={handleEditChange} />
                                                <label className="block text-gray-700 font-semibold text-sm mt-2 mb-1">아티스트</label>
                                                <input type="text" name="artist" className="w-full p-2 border border-gray-300 rounded-md text-sm text-gray-700 mb-1"
                                                    value={editedMusicData.artist} onChange={handleEditChange} />
                                                <label className="block text-gray-700 font-semibold text-sm mt-2 mb-1">앨범</label>
                                                <input type="text" name="album" className="w-full p-2 border border-gray-300 rounded-md text-sm text-gray-700 mb-1"
                                                    value={editedMusicData.album} onChange={handleEditChange} />
                                            </>
                                        ) : (
                                            <>
                                                <strong className="music-title block text-xl font-bold text-gray-900">{music.title}</strong>
                                                <small className="music-artist block text-base text-gray-700">아티스트: {Array.isArray(music.artist) ? music.artist.join(', ') : music.artist}</small>
                                                <small className="music-album block text-base text-gray-700">앨범: {music.album ? music.album : 'N/A'}</small>
                                            </>
                                        )}
                                        <small className="text-gray-600 block mt-1">
                                            업로드: {new Date(music.uploadDate).toLocaleString()}
                                            {music.duration ? ` | 재생 시간: ${formatDuration(music.duration)}` : ''}
                                        </small>
                                        <div className="lyrics-section mt-3 w-full">
                                            {editingMusicId === music.id ? (
                                                <>
                                                    <label className="block text-gray-700 font-semibold text-sm mb-1">가사</label>
                                                    <textarea name="lyrics" rows="5" placeholder="가사를 입력하세요." className="w-full p-2 border border-gray-300 rounded-md text-sm"
                                                        value={editedMusicData.lyrics} onChange={handleEditChange}></textarea>
                                                </>
                                            ) : (
                                                <>
                                                    <button
                                                        className="btn btn-sm bg-blue-100 hover:bg-blue-200 text-blue-800 text-sm py-1 px-3 rounded-md transition duration-150 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500"
                                                        onClick={() => toggleLyrics(music.id)}
                                                    >
                                                        가사 {music.lyrics && music.lyrics.trim() !== '' ? (expandedLyrics[music.id] ? '숨기기' : '보기') : '없음'}
                                                    </button>
                                                    <pre className={`music-lyrics-content mt-2 bg-gray-50 p-3 rounded-md max-h-48 overflow-y-auto text-sm text-gray-700 border border-gray-200 ${expandedLyrics[music.id] ? '' : 'hidden'}`}>
                                                        {music.lyrics && music.lyrics.trim() !== '' ? music.lyrics : '가사 정보가 없습니다.'}
                                                    </pre>
                                                </>
                                            )}
                                        </div>
                                    </div>
                                    <div className="music-actions flex flex-col md:flex-row items-center justify-end mt-4 md:mt-0 w-full md:w-auto">
                                        {music.coverImagePath && (
                                            <img
                                                src={`http://localhost:8485/api/files/cover-image/${music.coverImagePath.split('/').pop().split('\\').pop()}`}
                                                alt="앨범 커버"
                                                className="w-16 h-16 object-cover rounded-md mr-0 mb-3 md:mb-0 md:mr-4 shadow-sm"
                                                onError={(e) => { e.target.onerror = null; e.target.src = 'https://placehold.co/64x64/cccccc/000000?text=No+Cover'; }}
                                            />
                                        )}
                                        <audio id={`audio-${music.id}`} controls preload="none" className="w-full md:w-56 h-10 mt-2 md:mt-0 mb-3 md:mb-0 rounded-md">
                                            <source src={`${API_BASE_URL}/stream/${music.id}`} type="audio/mpeg" />
                                            <source src={`${API_BASE_URL}/stream/${music.id}`} type="audio/wav" />
                                            Your browser does not support the audio element.
                                        </audio>
                                        <div className="flex gap-2 mt-2 md:mt-0 md:ml-4 flex-wrap justify-center">
                                            {editingMusicId === music.id ? (
                                                <>
                                                    <button className="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded-md text-sm transition duration-150 ease-in-out"
                                                        onClick={() => saveEditedMusic(music.id)}>저장</button>
                                                    <button className="bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded-md text-sm transition duration-150 ease-in-out"
                                                        onClick={cancelEdit}>취소</button>
                                                </>
                                            ) : (
                                                <>
                                                    <button className="bg-indigo-500 hover:bg-indigo-600 text-white font-bold py-2 px-4 rounded-md text-sm transition duration-150 ease-in-out"
                                                        onClick={() => playMusic(music.id, music.title)}>재생</button>
                                                    <button className="bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-2 px-4 rounded-md text-sm transition duration-150 ease-in-out"
                                                        onClick={() => editMusic(music.id)}>수정</button>
                                                    <button className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-md text-sm transition duration-150 ease-in-out"
                                                        onClick={() => confirmDeleteMusic(music.id, music.title)}>삭제</button>
                                                </>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>

            {/* Confirmation Modal */}
            {showConfirmModal && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-sm mx-auto relative text-center">
                        <h5 className="text-xl font-bold text-gray-800 mb-5">확인</h5>
                        <p className="text-gray-600 mb-6">{confirmMessage}</p>
                        <div className="flex justify-center gap-4">
                            <button
                                className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={confirmAction}
                            >
                                예
                            </button>
                            <button
                                className="bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={() => setShowConfirmModal(false)}
                            >
                                아니오
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default MusicAdminPage;
