import React, { useState, useEffect, useRef } from 'react';
// import { useNavigate } from 'react-router-dom'; // 실제 앱에서는 React Router 사용 권장
// import apiClient from './api/apiClient'; // apiClient를 사용하는 경우 주석 해제
import { Music, Heart, Share, Pen, Trash } from 'lucide-react'; // 아이콘 라이브러리 (npm install lucide-react)

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
    if (seconds === null || isNaN(seconds)) {
        return '00:00';
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60);
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
};

function MusicDetailPage() {
    const [musicDetail, setMusicDetail] = useState(null);
    const [jwtToken, setJwtToken] = useState('');
    const [userInfo, setUserInfo] = useState(null); // 로그인한 사용자 정보
    const [showPlaylistModal, setShowPlaylistModal] = useState(false);
    const [playlists, setPlaylists] = useState([]);
    const [message, setMessage] = useState(''); // 일반 메시지
    const [isSuccessMessage, setIsSuccessMessage] = useState(false); // 메시지 타입 (성공/실패)
    const [showConfirmModal, setShowConfirmModal] = useState(false); // 삭제 확인 모달
    const [confirmAction, setConfirmAction] = useState(null); // 확인 모달에서 실행할 함수
    const [isLoadingPlaylists, setIsLoadingPlaylists] = useState(false);

    const audioPlayerRef = useRef(null); // 오디오 플레이어 ref
    // const navigate = useNavigate(); // 실제 앱에서는 React Router 사용

    // 메시지를 설정하고 일정 시간 후 사라지게 하는 함수
    const showTemporaryMessage = (msg, isSuccess = false) => {
        setMessage(msg);
        setIsSuccessMessage(isSuccess);
        setTimeout(() => {
            setMessage('');
        }, 3000); // 3초 후 메시지 사라짐
    };

    // 페이지 로드 시 JWT 토큰 및 음악 ID 가져오기
    useEffect(() => {
        const storedToken = getCookie('jwt_token');
        if (storedToken) {
            setJwtToken(storedToken);
        } else {
            showTemporaryMessage('JWT 토큰이 없습니다. 일부 기능(예: 플레이리스트 추가, 관리자 버튼)이 제한될 수 있습니다.', false);
        }

        const urlParams = new URLSearchParams(window.location.search);
        const musicId = urlParams.get('id');

        if (!musicId) {
            showTemporaryMessage('음악 ID가 제공되지 않았습니다. 메인 페이지로 이동합니다.', false);
            setTimeout(() => { window.location.href = '/main'; }, 2000); // 실제 앱에서는 navigate('/main')
            return;
        }
        fetchMusicDetail(musicId, storedToken);
        checkLoginStatus(storedToken);
    }, []);

    // 로그인 상태 확인 및 사용자 정보 가져오기
    const checkLoginStatus = async (token) => {
        try {
            const headers = {};
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            // apiClient 사용 시: const response = await apiClient.get('/api/user/me', { headers });
            const response = await fetch('/api/user/me', {
                method: 'GET',
                headers: headers,
                credentials: 'include',
                redirect: 'follow'
            });

            if (response.ok) {
                const userData = await response.json();
                setUserInfo(userData);
                return true;
            } else if (response.status === 401 || response.status === 403) {
                setUserInfo(null);
                setJwtToken('');
                return false;
            }
            return false;
        } catch (err) {
            console.error('로그인 상태 확인 중 오류 발생:', err);
            setUserInfo(null);
            setJwtToken('');
            return false;
        }
    };

    // 음악 상세 정보 로드 함수
    const fetchMusicDetail = async (musicId, token) => {
        try {
            const headers = {};
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            // apiClient 사용 시: const response = await apiClient.get(`${API_BASE_URL}/${musicId}`, { headers });
            const response = await fetch(`${API_BASE_URL}/${musicId}`, { headers: headers });

            if (!response.ok) {
                if (response.status === 404) {
                    showTemporaryMessage('음악을 찾을 수 없습니다.', false);
                } else if (response.status === 401 || response.status === 403) {
                    showTemporaryMessage('음악 정보를 볼 권한이 없거나 로그인 만료입니다. 로그인 후 다시 시도해주세요.', false);
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000); // 실제 앱에서는 navigate('/custom_login')
                } else {
                    showTemporaryMessage(`음악 정보를 불러오는데 실패했습니다: ${response.statusText}`, false);
                }
                throw new Error('Failed to fetch music detail.');
            }
            const music = await response.json();
            setMusicDetail(music);
            if (audioPlayerRef.current) {
                audioPlayerRef.current.src = `${API_BASE_URL}/stream/${music.id}`;
                audioPlayerRef.current.load();
            }
        } catch (error) {
            console.error('음악 정보 로딩 중 오류 발생:', error);
            // 메시지는 showTemporaryMessage 함수에서 이미 처리됨
        }
    };

    // 플레이리스트 목록을 모달에 로드하는 함수
    const loadPlaylistsIntoModal = async () => {
        setIsLoadingPlaylists(true);
        setPlaylists([]); // 기존 목록 초기화

        try {
            const headers = {};
            if (jwtToken) {
                headers['Authorization'] = `Bearer ${jwtToken}`;
            } else {
                showTemporaryMessage('플레이리스트를 보려면 로그인이 필요합니다.', false);
                setIsLoadingPlaylists(false);
                return;
            }

            // apiClient 사용 시: const response = await apiClient.get('/api/playlists/my', { headers });
            const response = await fetch('/api/playlists/my', { headers: headers });

            if (response.status === 401 || response.status === 403) {
                showTemporaryMessage('플레이리스트를 보거나 수정하려면 로그인이 필요합니다.', false);
                setTimeout(() => { window.location.href = '/custom_login'; }, 2000); // 실제 앱에서는 navigate('/custom_login')
                return;
            }
            if (!response.ok) {
                throw new Error('플레이리스트를 불러오는 데 실패했습니다: ' + response.statusText);
            }
            const data = await response.json();
            setPlaylists(data);
        } catch (error) {
            console.error('플레이리스트 로딩 중 오류 발생:', error);
            showTemporaryMessage('플레이리스트를 불러오는 데 실패했습니다: ' + error.message, false);
        } finally {
            setIsLoadingPlaylists(false);
        }
    };

    // 플레이리스트에 음악 추가 함수
    const addMusicToPlaylist = async (targetPlaylistId) => {
        if (!musicDetail) {
            showTemporaryMessage('음악 정보가 없습니다.', false);
            return;
        }

        try {
            const headers = {
                'Content-Type': 'application/json'
            };
            if (jwtToken) {
                headers['Authorization'] = `Bearer ${jwtToken}`;
            } else {
                showTemporaryMessage('플레이리스트에 음악을 추가하려면 로그인이 필요합니다.', false);
                setTimeout(() => { window.location.href = '/custom_login'; }, 2000); // 실제 앱에서는 navigate('/custom_login')
                return;
            }

            // apiClient 사용 시: const response = await apiClient.post(`/api/playlists/${targetPlaylistId}/music`, { musicId: musicDetail.id }, { headers });
            const response = await fetch(`/api/playlists/${targetPlaylistId}/music`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ musicId: musicDetail.id })
            });

            if (response.status === 400) { // Bad Request (e.g., already added)
                const errorData = await response.json();
                showTemporaryMessage('음악 추가 실패: ' + (errorData.message || '알 수 없는 오류'), false);
            } else if (!response.ok) {
                throw new Error('음악 추가에 실패했습니다: ' + response.statusText);
            } else {
                const updatedPlaylist = await response.json();
                showTemporaryMessage(`'${updatedPlaylist.title}' 플레이리스트에 음악이 성공적으로 추가되었습니다!`, true);
                setShowPlaylistModal(false); // 모달 닫기
            }
        } catch (error) {
            console.error('플레이리스트에 음악 추가 중 오류 발생:', error);
            showTemporaryMessage('음악 추가 중 오류가 발생했습니다: ' + error.message, false);
        }
    };

    // 음악 삭제 함수
    const handleDeleteMusic = async () => {
        if (!musicDetail || !musicDetail.id) {
            showTemporaryMessage('삭제할 음악 ID를 찾을 수 없습니다.', false);
            setShowConfirmModal(false); // 확인 모달 닫기
            return;
        }

        try {
            const headers = {};
            if (jwtToken) {
                headers['Authorization'] = `Bearer ${jwtToken}`;
            } else {
                showTemporaryMessage('음악을 삭제할 권한이 없거나 로그인 만료입니다. 로그인 후 다시 시도해주세요.', false);
                setTimeout(() => { window.location.href = '/custom_login'; }, 2000); // 실제 앱에서는 navigate('/custom_login')
                setShowConfirmModal(false); // 확인 모달 닫기
                return;
            }

            // apiClient 사용 시: const response = await apiClient.delete(`${API_BASE_URL}/${musicDetail.id}`, { headers });
            const response = await fetch(`${API_BASE_URL}/${musicDetail.id}`, { method: 'DELETE', headers: headers });

            if (response.ok) {
                showTemporaryMessage('음악이 성공적으로 삭제되었습니다.', true);
                setTimeout(() => { window.location.href = '/main'; }, 2000); // 실제 앱에서는 navigate('/main')
            } else if (response.status === 401 || response.status === 403) {
                showTemporaryMessage('음악을 삭제할 권한이 없습니다. 로그인 후 다시 시도하거나, 업로더에게 문의하세요.', false);
            } else {
                showTemporaryMessage('음악 삭제에 실패했습니다: ' + response.statusText, false);
            }
        } catch (error) {
            console.error('음악 삭제 중 오류 발생:', error);
            showTemporaryMessage('음악 삭제 중 오류가 발생했습니다.', false);
        } finally {
            setShowConfirmModal(false); // 확인 모달 닫기
        }
    };

    // 권한 확인 헬퍼
    const canEditOrDelete = userInfo && musicDetail &&
                            (userInfo.id === musicDetail.uploaderId || (userInfo.roles && userInfo.roles.includes('ROLE_ADMIN')));

    const coverImageUrl = musicDetail?.coverImagePath
                        ? `http://localhost:8485/api/files/cover-image/${musicDetail.coverImagePath.split('/').pop().split('\\').pop()}`
                        : '/images/default_album_cover.png';

    if (!musicDetail) {
        return (
            <div className="flex justify-center items-center h-screen bg-gray-100">
                <p className="text-gray-600 text-lg">음악 정보를 불러오는 중...</p>
                {message && (
                    <div className={`fixed top-4 right-4 p-4 rounded-md shadow-lg ${isSuccessMessage ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                        {message}
                    </div>
                )}
            </div>
        );
    }

    return (
        <div className="font-sans bg-gray-100 text-gray-800 min-h-screen pb-10">
            {/* Message Display */}
            {message && (
                <div className={`fixed top-4 right-4 z-50 p-4 rounded-md shadow-lg ${isSuccessMessage ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {message}
                </div>
            )}

            <div className="container max-w-4xl mx-auto mt-12 bg-white p-8 rounded-lg shadow-xl">
                <div className="music-detail-header text-center mb-8">
                    <img
                        id="musicCover"
                        src={coverImageUrl}
                        alt="앨범 커버"
                        className="w-64 h-64 object-cover rounded-lg mb-5 shadow-md mx-auto"
                        onError={(e) => { e.target.onerror = null; e.target.src = '/images/default_album_cover.png'; }}
                    />
                    <h2 id="musicTitle" className="text-3xl font-bold mb-2">{musicDetail.title}</h2>
                    <p id="musicArtist" className="text-xl text-gray-600 mb-1">
                        {Array.isArray(musicDetail.artist) ? musicDetail.artist.join(', ') : musicDetail.artist}
                    </p>
                    <p id="musicAlbum" className="text-lg text-gray-500">{musicDetail.album || '알 수 없음'}</p>
                </div>

                <div className="music-info text-left border-t border-b border-gray-200 py-4 mb-6">
                    <div className="info-item flex items-center mb-2">
                        <strong className="inline-block w-32 font-semibold text-gray-700">재생 시간:</strong>
                        <span id="musicDuration">{formatDuration(musicDetail.duration)}</span>
                    </div>
                    <div className="info-item flex items-center mb-2">
                        <strong className="inline-block w-32 font-semibold text-gray-700">업로드 날짜:</strong>
                        <span id="musicUploadDate">{new Date(musicDetail.uploadDate).toLocaleDateString()}</span>
                    </div>
                    <div className="info-item flex items-center mb-2">
                        <strong className="inline-block w-32 font-semibold text-gray-700">재생 횟수:</strong>
                        <span id="musicPlayCount">{musicDetail.playCount}</span>
                    </div>
                    <div className="info-item flex items-center">
                        <strong className="inline-block w-32 font-semibold text-gray-700">업로더:</strong>
                        <span id="musicUploaderNickname">{musicDetail.uploaderNickname || musicDetail.uploaderId || '알 수 없음'}</span>
                    </div>
                </div>

                <audio ref={audioPlayerRef} controls className="w-full mt-6 rounded-md shadow-sm">
                    <source src={`${API_BASE_URL}/stream/${musicDetail.id}`} type="audio/mpeg" />
                    Your browser does not support the audio element.
                </audio>

                <div className="flex flex-wrap justify-center gap-3 mt-6">
                    <button
                        id="addPlaylistBtn"
                        className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                        onClick={() => { setShowPlaylistModal(true); loadPlaylistsIntoModal(); }}
                    >
                        <Music className="inline-block w-5 h-5 mr-2" /> 플레이리스트에 추가
                    </button>
                    <button
                        id="likeBtn"
                        className="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                        onClick={() => showTemporaryMessage('좋아요 기능은 추후 구현됩니다.', false)}
                    >
                        <Heart className="inline-block w-5 h-5 mr-2" /> 좋아요
                    </button>
                    <button
                        id="shareBtn"
                        className="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                        onClick={() => showTemporaryMessage('공유 기능은 추후 구현됩니다.', false)}
                    >
                        <Share className="inline-block w-5 h-5 mr-2" /> 공유
                    </button>
                    {canEditOrDelete && (
                        <>
                            <button
                                id="editBtn"
                                className="bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={() => showTemporaryMessage('수정 기능은 추후 구현됩니다.', false)}
                            >
                                <Pen className="inline-block w-5 h-5 mr-2" /> 수정
                            </button>
                            <button
                                id="deleteBtn"
                                className="bg-red-700 hover:bg-red-800 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={() => { setShowConfirmModal(true); setConfirmAction(() => handleDeleteMusic); }}
                            >
                                <Trash className="inline-block w-5 h-5 mr-2" /> 삭제
                            </button>
                        </>
                    )}
                    <button
                        onClick={() => window.location.href = '/main'} // 실제 앱에서는 navigate('/main')
                        className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                    >
                        메인으로
                    </button>
                </div>

                <div className="lyrics-section mt-8 p-6 bg-gray-100 rounded-lg text-left whitespace-pre-wrap max-h-96 overflow-y-auto border border-gray-200 shadow-inner">
                    <h4 id="lyricsTitleHeader" className="text-xl font-bold text-gray-700 mb-4 text-center">{musicDetail.title} 가사</h4>
                    <div id="musicLyrics" className="text-gray-700">
                        {musicDetail.lyrics && musicDetail.lyrics.trim() !== '' ? musicDetail.lyrics : '등록된 가사가 없습니다.'}
                    </div>
                </div>
            </div>

            {/* Playlist Add Modal */}
            {showPlaylistModal && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md mx-auto relative">
                        <button
                            className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 text-2xl"
                            onClick={() => setShowPlaylistModal(false)}
                        >
                            &times;
                        </button>
                        <h5 className="text-xl font-bold text-gray-800 mb-5 text-center">플레이리스트 선택</h5>
                        <p className="text-gray-600 mb-4 text-center">음악을 추가할 플레이리스트를 선택해주세요.</p>
                        <ul className="list-none p-0 my-4 border border-gray-200 rounded-md max-h-64 overflow-y-auto">
                            {isLoadingPlaylists ? (
                                <li className="text-center text-gray-500 py-4">플레이리스트를 불러오는 중...</li>
                            ) : playlists.length === 0 ? (
                                <li className="text-center text-gray-500 py-4">생성된 플레이리스트가 없습니다.</li>
                            ) : (
                                playlists.map(playlist => (
                                    <li key={playlist.id} className="flex justify-between items-center px-4 py-3 border-b border-gray-200 last:border-b-0 hover:bg-gray-50">
                                        <span className="text-gray-700">{playlist.title}</span>
                                        <button
                                            type="button"
                                            className="bg-blue-500 hover:bg-blue-600 text-white text-sm py-1 px-3 rounded-md transition duration-150 ease-in-out"
                                            onClick={() => addMusicToPlaylist(playlist.id)}
                                        >
                                            추가
                                        </button>
                                    </li>
                                ))
                            )}
                        </ul>
                        <div className="text-right mt-4">
                            <button
                                type="button"
                                className="bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={() => setShowPlaylistModal(false)}
                            >
                                닫기
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Confirmation Modal */}
            {showConfirmModal && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-sm mx-auto relative text-center">
                        <h5 className="text-xl font-bold text-gray-800 mb-5">확인</h5>
                        <p className="text-gray-600 mb-6">이 작업을 정말로 실행하시겠습니까? 되돌릴 수 없습니다.</p>
                        <div className="flex justify-center gap-4">
                            <button
                                className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={() => {
                                    if (confirmAction) confirmAction();
                                }}
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

export default MusicDetailPage;
