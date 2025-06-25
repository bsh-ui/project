import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom'; // Link와 useNavigate 임포트
import apiClient from './api/apiClient'; // apiClient 임포트

// API 기본 URL 설정 (apiClient에서 관리되므로 여기서는 사용되지 않음. 참고용으로 남겨둠)
// const API_BASE_URL = 'http://localhost:8485/api/music';

// JWT 토큰을 쿠키에서 가져오는 헬퍼 함수 (apiClient의 인터셉터가 JWT 토큰을 localStorage에서 가져오도록 설정했다면 이 함수는 불필요)
// 이 프로젝트의 다른 컴포넌트들이 여전히 이 함수를 사용할 수 있으므로 일단 유지합니다.
const getCookie = (name) => {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for(let i=0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) {
            const foundValue = c.substring(nameEQ.length, c.length);
            return foundValue;
        }
    }
    return null;
};

// duration (초)를 "분:초" 형식으로 변환하는 헬퍼 함수
const formatDuration = (seconds) => {
    if (typeof seconds !== 'number' || isNaN(seconds) || seconds < 0) {
        return '0:00'; // 유효하지 않은 값 처리
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60);
    return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
};

function MainPage() {
    const navigate = useNavigate(); // useNavigate 훅 사용

    // 상태 관리
    const [jwtToken, setJwtToken] = useState('');
    const [userInfo, setUserInfo] = useState(null);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchResults, setSearchResults] = useState({ content: [], pageable: {}, totalPages: 0, first: true, last: true });
    const [publicMusicList, setPublicMusicList] = useState({ content: [], pageable: {}, totalPages: 0, first: true, last: true });
    const [currentPageSearch, setCurrentPageSearch] = useState(0); // 검색 결과 페이지
    const [currentPagePublic, setCurrentPagePublic] = useState(0); // 공개 음악 목록 페이지
    const [submissionMessage, setSubmissionMessage] = useState(''); // 성공/실패 메시지 (alert 대체)
    const [isSuccessMessage, setIsSuccessMessage] = useState(false); // 메시지 타입 (성공/실패)
    const [isLoadingPublicMusic, setIsLoadingPublicMusic] = useState(true); // 공개 음악 로딩 상태
    const [isLoadingSearchResults, setIsLoadingSearchResults] = useState(false); // 검색 결과 로딩 상태

    // 플레이어 관련 상태 및 ref
    const audioRefs = useRef({}); // 각 음악 ID에 대한 Audio 요소를 저장
    const [currentPlayingMusicId, setCurrentPlayingMusicId] = useState(null); // 현재 재생 중인 음악 ID

    // 플레이리스트 모달 상태
    const [showPlaylistModal, setShowPlaylistModal] = useState(false);
    const [playlistTitle, setPlaylistTitle] = useState('');
    const [playlistDescription, setPlaylistDescription] = useState('');
    const [playlistMessage, setPlaylistMessage] = useState(''); // 플레이리스트 모달 내 메시지

    // 드롭다운 메뉴 상태 (Bootstrap 드롭다운 대체)
    const [openDropdown, setOpenDropdown] = useState(null); // 현재 열린 드롭다운 ID

    const handleDropdownToggle = (id) => {
        setOpenDropdown(openDropdown === id ? null : id);
    };

    // 컴포넌트 마운트 시 JWT 토큰 및 초기 음악 목록 불러오기
    useEffect(() => {
        const storedToken = localStorage.getItem('jwtToken'); // localStorage에서 토큰 가져오도록 수정
        if (storedToken) {
            setJwtToken(storedToken);
            console.log('JWT 토큰을 localStorage에서 성공적으로 가져왔습니다.');
        } else {
            console.warn('JWT 토큰이 localStorage에 없습니다.');
        }
        console.log('[useEffect] Fetching public music list on mount.');
        fetchPublicMusicList(0); // 첫 페이지 로드
        checkLoginStatus(); // 사용자 정보 확인
    }, []); // 빈 배열은 컴포넌트 마운트 시 한 번만 실행됨을 의미합니다.

    // 로그인 상태 확인 함수
    const checkLoginStatus = async () => {
        try {
            // apiClient 사용
            const response = await apiClient.get('/api/user/me');

            if (response.status === 200) { // axios는 2xx 응답을 기본 성공으로 처리
                const userData = response.data;
                setUserInfo(userData);
                return true;
            }
            return false; // 2xx 외의 응답 (apiClient 인터셉터에서 처리되겠지만, 명시적으로)
        } catch (err) {
            console.error('로그인 상태 확인 중 오류 발생:', err);
            setUserInfo(null);
            setJwtToken('');
            // apiClient 인터셉터가 401/403을 처리할 것이므로, 여기서는 메시지만 설정
            setSubmissionMessage('사용자 정보를 불러오는 데 실패했거나 세션이 만료되었습니다.');
            setIsSuccessMessage(false);
            return false;
        }
    };

    // 공개용 음악 목록을 불러오는 함수
    const fetchPublicMusicList = async (page = 0) => {
        setIsLoadingPublicMusic(true);
        setSubmissionMessage(''); // 메시지 초기화
        setIsSuccessMessage(false);

        try {
            // apiClient 사용, GET 요청 시 파라미터는 params 객체로 전달
            const response = await apiClient.get('/api/music', {
                params: {
                    page: page,
                    size: 12,
                    sort: 'uploadDate,desc'
                }
            });

            // axios 응답은 response.data에 실제 데이터가 있음
            const pageData = response.data;
            setPublicMusicList(pageData);
            setCurrentPagePublic(pageData.pageable.pageNumber);
        } catch (error) {
            console.error('음악 목록을 불러오는 데 실패했습니다:', error);
            // apiClient 인터셉터에서 401/403은 처리될 것이므로, 그 외의 에러 처리
            if (error.response && error.response.data && error.response.data.message) {
                setSubmissionMessage(`음악 목록 불러오기 실패: ${error.response.data.message}`);
            } else {
                setSubmissionMessage(`음악 목록을 불러오는 데 실패했습니다: ${error.message}`);
            }
            setIsSuccessMessage(false);
        } finally {
            setIsLoadingPublicMusic(false);
        }
    };

    // 음악 검색 함수 (메인 페이지 검색 바용)
    const searchMusic = async (page = 0) => {
        if (!searchKeyword.trim()) {
            setSubmissionMessage('검색어를 입력하세요.');
            setIsSuccessMessage(false);
            return;
        }
        
        setIsLoadingSearchResults(true);
        setSubmissionMessage(''); // 메시지 초기화
        setIsSuccessMessage(false);

        try {
            // apiClient 사용, GET 요청 시 파라미터는 params 객체로 전달
            const response = await apiClient.get('/api/music/search', {
                params: {
                    keyword: searchKeyword,
                    page: page,
                    size: 10,
                    sort: 'uploadDate,desc'
                }
            });

            const data = response.data;
            setSearchResults(data);
            setCurrentPageSearch(data.pageable.pageNumber);
        } catch (error) {
            console.error('검색 중 오류 발생:', error);
            if (error.response && error.response.data && error.response.data.message) {
                setSubmissionMessage(`검색 실패: ${error.response.data.message}`);
            } else {
                setSubmissionMessage(`검색 중 오류가 발생했습니다: ${error.message}`);
            }
            setIsSuccessMessage(false);
        } finally {
            setIsLoadingSearchResults(false);
        }
    };

    // 음악 재생/일시정지 토글 함수
    const togglePlayPause = async (musicId) => {
        const isLoggedIn = await checkLoginStatus();

        if (!isLoggedIn) {
            setSubmissionMessage('스트리밍을 위해서는 로그인이 필요합니다. 로그인 페이지로 이동합니다.');
            setIsSuccessMessage(false);
            navigate('/custom_login'); // navigate 사용
            return;
        }

        const audio = audioRefs.current[musicId];

        if (!audio) {
            console.error('Audio element not found for music ID:', musicId);
            setSubmissionMessage('음악 파일을 찾을 수 없습니다.');
            setIsSuccessMessage(false);
            return;
        }

        if (currentPlayingMusicId === musicId && !audio.paused) {
            // 현재 재생 중인 음악을 일시정지
            audio.pause();
            setCurrentPlayingMusicId(null);
        } else {
            // 다른 음악이 재생 중이면 중지
            if (currentPlayingMusicId && audioRefs.current[currentPlayingMusicId] && currentPlayingMusicId !== musicId) {
                audioRefs.current[currentPlayingMusicId].pause();
            }
            // 새 음악 재생
            try {
                await audio.play();
                setCurrentPlayingMusicId(musicId);
            } catch (error) {
                console.error('음악 재생 실패:', error);
                setSubmissionMessage('음악 재생에 실패했습니다: ' + error.message);
                setIsSuccessMessage(false);
            }
        }
    };

    // 플레이리스트 생성 모달 제출 핸들러
    const handleCreatePlaylistSubmit = async (event) => {
        event.preventDefault();
        setPlaylistMessage(''); // 메시지 초기화

        if (!playlistTitle.trim()) {
            setPlaylistMessage('플레이리스트 제목은 필수입니다.');
            return;
        }

        try {
            // ⭐⭐ 수정된 부분: 플레이리스트 생성 API 호출로 변경 ⭐⭐
            const response = await apiClient.post('/api/playlists', {
                title: playlistTitle,
                description: playlistDescription
            });

            const newPlaylist = response.data;
            setSubmissionMessage(`플레이리스트 '${newPlaylist.title}'이(가) 성공적으로 생성되었습니다.`);
            setIsSuccessMessage(true);
            setShowPlaylistModal(false); // 모달 닫기
            setPlaylistTitle(''); // 폼 초기화
            setPlaylistDescription(''); // 폼 초기화
            // 플레이리스트 생성 후 필요하다면 목록 새로고침 등
        } catch (error) {
            console.error('플레이리스트 생성 실패:', error);
            if (error.response && error.response.data && error.response.data.message) {
                setPlaylistMessage(`플레이리스트 생성 실패: ${error.response.data.message}`);
            } else {
                setPlaylistMessage('네트워크 오류 또는 서버 응답 문제 발생.');
            }
        }
    };

    const handleLogout = async () => {
        try {
            // 실제 백엔드 로그아웃 API 호출 (예: 세션 무효화)
            // 백엔드에 /api/logout 엔드포인트가 POST 요청을 받는다고 가정
            await apiClient.post('/api/logout'); 
            setSubmissionMessage('로그아웃 되었습니다.');
            setIsSuccessMessage(true);
        } catch (error) {
            console.error('로그아웃 중 오류 발생:', error);
            setSubmissionMessage('로그아웃 중 오류가 발생했습니다.');
            setIsSuccessMessage(false);
        } finally {
            localStorage.removeItem('jwtToken'); // JWT 토큰 삭제
            setJwtToken('');
            setUserInfo(null);
            navigate('/custom_login'); // navigate 사용
        }
    };

    return (
        <div className="font-sans text-center bg-gray-100 min-h-screen">
            {/* Header */}
            <header className="bg-white shadow-md py-4 px-6 flex flex-col md:flex-row items-center justify-between sticky top-0 z-10">
                <div className="flex items-center justify-between w-full md:w-auto mb-4 md:mb-0">
                    <div className="text-2xl font-bold text-gray-800 logo">ListenIt</div> {/* 로고 */}
                </div>
                <div className="search-bar flex items-center gap-2 w-full md:w-auto">
                    <input
                        type="text"
                        id="keywordInput"
                        placeholder="여름 밤을 담은 감성 시티팝  ･*:.｡.☆彡"
                        value={searchKeyword}
                        onChange={(e) => setSearchKeyword(e.target.value)}
                        onKeyPress={(e) => { if (e.key === 'Enter') searchMusic(0); }}
                        className="p-2 px-4 border border-gray-300 rounded-full w-full md:w-80 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                        type="button"
                        className="bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-full cursor-pointer transition duration-150 ease-in-out flex items-center gap-1"
                        onClick={() => searchMusic(0)}
                    >
                        <i className="bi bi-search"></i> 검색
                    </button>
                </div>
            </header>

            {/* Navigation */}
            <nav className="bg-gray-800 text-white py-3 shadow-md">
                <ul className="flex flex-wrap justify-center space-x-4 md:space-x-8 text-lg">
                    {/* 차트100 */}
                    <li className="relative group">
                        <a href="#" onClick={() => handleDropdownToggle('chart')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out">
                            차트100
                        </a>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'chart' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">일간 차트</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">주간 차트</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">월간 차트</a></li>
                        </ul>
                    </li>
                    {/* 최신음악 */}
                    <li className="relative group">
                        <a href="#" onClick={() => handleDropdownToggle('newMusic')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out">
                            최신음악
                        </a>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'newMusic' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">최신 곡</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">최신 앨범</a></li>
                        </ul>
                    </li>
                    {/* 장르음악 */}
                    <li className="relative group">
                        <a href="#" onClick={() => handleDropdownToggle('genre')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out">
                            장르음악
                        </a>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'genre' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">발라드</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">댄스</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">힙합</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">록</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">재즈</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">POP</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">R&B</a></li>
                        </ul>
                    </li>
                    {/* 공지사항 */}
                    <li className="relative group">
                        <a href="#" onClick={() => handleDropdownToggle('notice')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out">
                            공지사항
                        </a>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'notice' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/notice_list">일반 공지</Link></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">업데이트 소식</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">이벤트</a></li>
                        </ul>
                    </li>
                    {/* 추천 */}
                    <li className="relative group">
                        <a href="#" onClick={() => handleDropdownToggle('recommend')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out">
                            추천
                        </a>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'recommend' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">맞춤 추천</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">에디터 추천</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">인기 플레이리스트</a></li>
                        </ul>
                    </li>
                     {/* 자유게시판 */}
                    <li className="relative group">
                        <a href="#" onClick={() => handleDropdownToggle('freeboard')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out">
                            자유게시판
                        </a>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'freeboard' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/board">인기 글</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/board/posts/new">새 글 작성</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/board">내 글 보기</Link></li>
                        </ul>
                    </li>
                    {/* 뮤직허그 */}
                    <li className="relative group">
                        <a href="#" onClick={() => handleDropdownToggle('musicHug')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out">
                            뮤직허그
                        </a>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'musicHug' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">실시간 듣기</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">친구 초대</a></li>
                            <li><a className="block px-4 py-2 hover:bg-gray-100 rounded-md" href="#">내 허그룸</a></li>
                        </ul>
                    </li>
                </ul>
            </nav>

            <div className="container p-8 rounded-lg max-w-5xl mx-auto mt-12 bg-white shadow-lg">
                <h1 className="text-3xl font-bold text-gray-800 mb-4">환영합니다!</h1>
                <p className="text-gray-600 mb-6">이곳은 메인 페이지입니다. 성공적으로 로그인되었습니다.</p>

                {submissionMessage && (
                    <div className={`mb-4 p-3 rounded-md text-center font-bold ${isSuccessMessage ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                        {submissionMessage}
                    </div>
                )}

                {/* User Info */}
                <div className="user-info mt-5 p-4 bg-blue-50 rounded-md text-left shadow-sm">
                    <h3 className="text-xl font-semibold text-gray-700 mb-2">사용자 정보:</h3>
                    {userInfo ? (
                        <>
                            <p className="text-gray-600"><strong>이름:</strong> <span>{userInfo.username}</span></p>
                            <p className="text-gray-600"><strong>이메일:</strong> <span>{userInfo.email}</span></p>
                            <p className="text-gray-600"><strong>닉네임:</strong> <span>{userInfo.nickname}</span></p>
                        </>
                    ) : (
                        <p className="text-gray-600">사용자 정보를 불러올 수 없습니다.</p>
                    )}
                </div>

                <p className="mt-6">
                    <button
                        onClick={handleLogout}
                        className="text-blue-600 hover:underline cursor-pointer bg-transparent border-none p-0 text-base"
                    >
                        로그아웃
                    </button>
                </p>

                {/* Music List Section (Replaced from music_list.html) */}
                <div className="mt-8">
                    <h2 className="text-2xl font-bold text-gray-800 mb-4">음악 목록</h2>
                    <div className="text-center text-muted mb-4">
                        {isLoadingPublicMusic && '음악을 불러오는 중...'}
                    </div>
                    {!isLoadingPublicMusic && publicMusicList.content.length === 0 && (
                        <p className="text-center text-gray-500">등록된 음악이 없습니다.</p>
                    )}
                    <div id="public-music-list-container" className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                        {publicMusicList.content.map(music => (
                            <div key={music.id} className="card bg-white rounded-lg shadow-md overflow-hidden transform transition-transform duration-200 hover:scale-105">
                                <Link to={`/music_detail?id=${music.id}`} className="block"> {/* Link 사용 */}
                                    <img
                                        src={music.coverImagePath
                                            ? `http://localhost:8485/api/files/cover-image/${music.coverImagePath.split('/').pop().split('\\').pop()}`
                                            : '/images/default_album_cover.png'}
                                        alt="앨범 커버"
                                        onError={(e) => { e.target.onerror = null; e.target.src = '/images/default_album_cover.png'; }}
                                        className="w-full h-48 object-cover"
                                    />
                                </Link>
                                <div className="p-4 flex flex-col justify-between items-start h-auto">
                                    <Link to={`/music_detail?id=${music.id}`} className="block text-left mb-2 w-full"> {/* Link 사용 */}
                                        <h5 className="text-lg font-semibold text-gray-900 truncate">{music.title}</h5>
                                        <p className="text-sm text-gray-600 truncate">
                                            {Array.isArray(music.artist) ? music.artist.join(', ') : music.artist}
                                        </p>
                                        <p className="text-xs text-gray-500 truncate">{music.album ? music.album : 'N/A'}</p>
                                        <small className="text-xs text-gray-400">
                                            {music.duration ? '재생 시간: ' + formatDuration(music.duration) : ''}
                                        </small>
                                    </Link>
                                    <div className="w-full mt-2">
                                        <audio
                                            ref={el => audioRefs.current[music.id] = el}
                                            preload="none"
                                            style={{ display: 'none' }}
                                            onEnded={() => setCurrentPlayingMusicId(null)}
                                        >
                                            <source src={`http://localhost:8485/api/music/stream/${music.id}`} type="audio/mpeg" />
                                            Your browser does not support the audio element.
                                        </audio>
                                        <button
                                            className="w-full bg-gray-800 hover:bg-gray-700 text-white py-2 rounded-md transition duration-150 ease-in-out flex items-center justify-center gap-2"
                                            onClick={() => togglePlayPause(music.id)}
                                        >
                                            {currentPlayingMusicId === music.id ? <i className="bi bi-pause-fill"></i> : <i className="bi bi-play-fill"></i>}
                                            {currentPlayingMusicId === music.id ? ' 일시정지' : ' 재생'}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                     <div className="flex justify-center items-center mt-6 space-x-4">
                        <button
                            onClick={() => fetchPublicMusicList(currentPagePublic - 1)}
                            disabled={publicMusicList.first}
                            className="bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition duration-150 ease-in-out"
                        >
                            이전 페이지
                        </button>
                        <span className="text-gray-700 font-medium">페이지 {currentPagePublic + 1} / {publicMusicList.totalPages}</span>
                        <button
                            onClick={() => fetchPublicMusicList(currentPagePublic + 1)}
                            disabled={publicMusicList.last}
                            className="bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition duration-150 ease-in-out"
                        >
                            다음 페이지
                        </button>
                    </div>
                </div>

                {/* Music Search Results */}
                <h2 className="text-2xl font-bold text-gray-800 mt-10 mb-4">음악 검색 결과</h2>
                <div id="resultsContainer" className="border border-gray-300 p-4 rounded-md min-h-[100px] text-left">
                    {isLoadingSearchResults ? (
                        <p className="text-center text-muted">검색 결과를 불러오는 중...</p>
                    ) : searchResults.content.length > 0 ? (
                        searchResults.content.map(music => (
                            <div key={music.id} className="music-item border-b border-dashed border-gray-200 py-3 last:border-b-0 flex items-center justify-between">
                                <div>
                                    <h3 className="text-lg font-semibold text-gray-800">{music.title}</h3>
                                    <p className="text-sm text-gray-600"><strong>아티스트:</strong> {Array.isArray(music.artist) ? music.artist.join(', ') : music.artist}</p>
                                    <p className="text-sm text-gray-600"><strong>앨범:</strong> {music.album || '정보 없음'}</p>
                                    <p className="text-xs text-gray-500"><strong>업로드 날짜:</strong> {new Date(music.uploadDate).toLocaleString()}</p>
                                    <p className="text-xs text-gray-500"><strong>재생 시간:</strong> {formatDuration(music.duration)}</p>
                                </div>
                                <div>
                                    <audio
                                        ref={el => audioRefs.current[music.id] = el}
                                        preload="none"
                                        style={{ display: 'none' }}
                                        onEnded={() => setCurrentPlayingMusicId(null)}
                                    >
                                        <source src={`http://localhost:8485/api/music/stream/${music.id}`} type="audio/mpeg" />
                                        Your browser does not support the audio element.
                                    </audio>
                                    <button
                                        onClick={() => togglePlayPause(music.id)}
                                        className="mt-2 py-2 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-md cursor-pointer transition duration-150 ease-in-out flex items-center justify-center gap-1"
                                    >
                                        {currentPlayingMusicId === music.id ? <i className="bi bi-pause-fill"></i> : <i className="bi bi-play-fill"></i>}
                                        {currentPlayingMusicId === music.id ? ' 일시정지' : ' 재생'}
                                    </button>
                                </div>
                            </div>
                        ))
                    ) : (
                        <p className="text-center text-gray-500">검색 결과가 없습니다.</p>
                    )}
                </div>

                <div id="paginationContainer" className="flex justify-center items-center mt-6 space-x-4">
                    <button
                        onClick={() => searchMusic(currentPageSearch - 1)}
                        disabled={searchResults.first}
                        className="bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition duration-150 ease-in-out"
                    >
                        이전 페이지
                    </button>
                    <span className="text-gray-700 font-medium">페이지 {currentPageSearch + 1} / {searchResults.totalPages}</span>
                    <button
                        onClick={() => searchMusic(currentPageSearch + 1)}
                        disabled={searchResults.last}
                        className="bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition duration-150 ease-in-out"
                    >
                        다음 페이지
                    </button>
                </div>

                <div className="signup-link mt-8 text-gray-700">
                    <p className="mb-2">계정이 없으신가요? <Link to="/signup" className="text-blue-600 hover:underline">회원가입</Link></p>
                </div>
                <div>
                    <p className="mt-4"><Link to="/mypage" className="text-blue-600 hover:underline">마이페이지</Link></p>
                </div>

                {userInfo && userInfo.roles && userInfo.roles.includes('ROLE_ADMIN') && (
                    <div className="mt-4">
                        <Link to="/music_upload" className="text-purple-600 hover:underline font-bold">음악 업로드 페이지</Link>
                    </div>
                )}

                {/* 플레이리스트 생성 버튼 (추가) */}
                <div className="mt-8">
                    <button
                        onClick={() => setShowPlaylistModal(true)}
                        className="bg-purple-600 hover:bg-purple-700 text-white py-2 px-5 rounded-md cursor-pointer transition duration-150 ease-in-out"
                    >
                        새 플레이리스트 만들기
                    </button>
                </div>

                {/* Playlist Creation Modal */}
                {showPlaylistModal && (
                    <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50">
                        <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md mx-auto relative">
                            <button
                                className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 text-2xl"
                                onClick={() => setShowPlaylistModal(false)}
                            >
                                &times;
                            </button>
                            <h3 className="text-2xl font-bold text-gray-800 mb-6 text-center">플레이리스트 생성</h3>
                            <form onSubmit={handleCreatePlaylistSubmit}>
                                <div className="mb-4">
                                    <label htmlFor="playlistTitle" className="block text-gray-700 text-sm font-bold mb-2 text-left">제목:</label>
                                    <input
                                        type="text"
                                        id="playlistTitle"
                                        value={playlistTitle}
                                        onChange={(e) => setPlaylistTitle(e.target.value)}
                                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                                        required
                                    />
                                </div>
                                <div className="mb-6">
                                    <label htmlFor="playlistDescription" className="block text-gray-700 text-sm font-bold mb-2 text-left">설명 (선택 사항):</label>
                                    <textarea
                                        id="playlistDescription"
                                        value={playlistDescription}
                                        onChange={(e) => setPlaylistDescription(e.target.value)}
                                        rows="3"
                                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                                    ></textarea>
                                </div>
                                {playlistMessage && (
                                    <div className="mb-4 p-3 rounded-md text-center bg-yellow-100 text-yellow-700 font-medium">
                                        {playlistMessage}
                                    </div>
                                )}
                                <div className="flex items-center justify-between">
                                    <button
                                        type="submit"
                                        className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transition duration-150 ease-in-out w-full"
                                    >
                                        생성
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default MainPage;
