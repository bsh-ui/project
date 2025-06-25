import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';
import { useAuth } from '../context/AuthContext';

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
    
    const navigate = useNavigate();
    const { isAuthenticated, user: authUser, logout: authLogout, loading: authLoading  } = useAuth();
    // 상태 관리
    const [searchKeyword, setSearchKeyword] = useState('');
    // ⭐ publicMusicList 대신 musicList와 pageInfo로 분리하여 관리 ⭐
    const [musicList, setMusicList] = useState([]); // 실제 음악 목록 데이터
    const [pageInfo, setPageInfo] = useState({ // 페이지네이션 정보
        pageNumber: 0,
        pageSize: 12, // 기본 페이지 크기를 12로 설정
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true
    });
    const [searchResults, setSearchResults] = useState({ content: [], pageable: {}, totalPages: 0, first: true, last: true });
    // const [currentPagePublic, setCurrentPagePublic] = useState(0); // pageInfo.pageNumber로 대체됨
    const [currentPageSearch, setCurrentPageSearch] = useState(0); // 검색 결과 페이지
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

    // 드롭다운 메뉴 상태
    const [openDropdown, setOpenDropdown] = useState(null); // 현재 열린 드롭다운 ID

    const handleDropdownToggle = (id) => {
        setOpenDropdown(openDropdown === id ? null : id);
    };

    // 임시 메시지 표시 헬퍼 함수
    const showTemporaryMessage = useCallback((msg, isSuccess = false) => {
        setSubmissionMessage(msg);
        setIsSuccessMessage(isSuccess);
        const timeoutId = setTimeout(() => {
            setSubmissionMessage('');
        }, 5000); // 5초 후 메시지 사라짐
        return () => clearTimeout(timeoutId);
    }, []); // showTemporaryMessage는 의존성이 없으므로 한 번만 생성

    // 공개용 음악 목록을 불러오는 함수 (useCallback으로 래핑)
    const fetchPublicMusicList = useCallback(async (page = 0) => {
        setIsLoadingPublicMusic(true);
        showTemporaryMessage(''); // 메시지 초기화

        try {
            const response = await apiClient.get('/api/music', {
                params: {
                    page: page,
                    size: pageInfo.pageSize, // ⭐ pageInfo의 pageSize 사용 ⭐
                    sort: 'uploadDate,desc'
                }
            });

            const responseData = response.data;
            // ⭐ 응답 데이터 유효성 검사 및 안전한 상태 업데이트 ⭐
            if (responseData && Array.isArray(responseData.content)) {
                setMusicList(responseData.content);
                setPageInfo({
                    pageNumber: responseData.pageable?.pageNumber ?? 0,
                    pageSize: responseData.pageable?.pageSize ?? pageInfo.pageSize,
                    totalElements: responseData.totalElements ?? 0,
                    totalPages: responseData.totalPages ?? 0,
                    first: responseData.first ?? true,
                    last: responseData.last ?? true
                });
            } else {
                // 데이터 형식이 예상과 다를 경우 빈 배열 및 기본 페이지 정보로 초기화
                setMusicList([]);
                setPageInfo({
                    pageNumber: 0,
                    pageSize: pageInfo.pageSize,
                    totalElements: 0,
                    totalPages: 0,
                    first: true,
                    last: true
                });
                console.warn('API 응답 데이터 형식이 올바르지 않거나 content가 누락되었습니다:', responseData);
            }
        } catch (error) {
            console.error('음악 목록을 불러오는 데 실패했습니다:', error);
            const errorMessage = error.response?.data?.message || `음악 목록을 불러오는 데 실패했습니다: ${error.message}`;
            showTemporaryMessage(errorMessage, false);
            setMusicList([]); // 오류 발생 시 음악 목록 비움
            setPageInfo({ // 오류 발생 시 페이지 정보도 초기화
                pageNumber: 0,
                pageSize: pageInfo.pageSize,
                totalElements: 0,
                totalPages: 0,
                first: true,
                last: true
            });
        } finally {
            setIsLoadingPublicMusic(false);
        }
    }, [showTemporaryMessage, pageInfo.pageSize]); // ⭐ pageInfo.pageSize를 의존성으로 추가 ⭐

    // 음악 검색 함수 (메인 페이지 검색 바용) (useCallback으로 래핑)
    const searchMusic = useCallback(async (page = 0) => {
        if (!searchKeyword.trim()) {
            showTemporaryMessage('검색어를 입력하세요.', false);
            setSearchResults({ content: [], pageable: {}, totalPages: 0, first: true, last: true }); // 검색 결과 초기화
            return;
        }

        setIsLoadingSearchResults(true);
        showTemporaryMessage(''); // 메시지 초기화

        try {
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
            const errorMessage = error.response?.data?.message || `검색 중 오류가 발생했습니다: ${error.message}`;
            showTemporaryMessage(errorMessage, false);
        } finally {
            setIsLoadingSearchResults(false);
        }
    }, [searchKeyword, showTemporaryMessage]); // searchKeyword와 showTemporaryMessage를 의존성으로 추가

    // 음악 재생/일시정지 토글 함수
    const togglePlayPause = async (musicId) => {
        if (!isAuthenticated) { // AuthContext의 isAuthenticated 사용
            showTemporaryMessage('스트리밍을 위해서는 로그인이 필요합니다. 로그인 페이지로 이동합니다.', false);
            navigate('/login'); // /custom_login 대신 /login 사용 (App.js 라우트에 따라)
            return;
        }

        const audio = audioRefs.current[musicId];

        if (!audio) {
            console.error('Audio element not found for music ID:', musicId);
            showTemporaryMessage('음악 파일을 찾을 수 없습니다.', false);
            return;
        }

        if (currentPlayingMusicId === musicId && !audio.paused) {
            audio.pause();
            setCurrentPlayingMusicId(null);
        } else {
            // 다른 음악이 재생 중이면 중지
            if (currentPlayingMusicId && audioRefs.current[currentPlayingMusicId] && currentPlayingMusicId !== musicId) {
                audioRefs.current[currentPlayingMusicId].pause();
            }
            // 새 음악 재생
            try {
                // 스트리밍 API는 로그인된 사용자만 접근 가능하므로 별도의 토큰 체크는 필요 없음 (apiClient 인터셉터가 처리)
                audio.src = `http://localhost:8485/api/music/stream/${musicId}`;
                audio.load(); // 새로운 소스를 로드
                await audio.play();
                setCurrentPlayingMusicId(musicId);
            } catch (error) {
                console.error('음악 재생 실패:', error);
                showTemporaryMessage('음악 재생에 실패했습니다: ' + error.message, false);
            }
        }
    };

    // 컴포넌트 마운트 시 초기 음악 목록 불러오기
    useEffect(() => {
        console.log('[MainPage useEffect] AuthLoading status:', authLoading);
        // AuthContext의 초기 로딩이 완료된 후에만 음악 목록을 불러옵니다.
        if (!authLoading) {
            console.log('[MainPage useEffect] Auth loading finished. Fetching public music list.');
            // 현재 URL의 페이지 파라미터를 읽어와서 페이지네이션 상태를 설정합니다.
            const params = new URLSearchParams(window.location.search);
            // Spring Data JPA는 페이지를 0부터 시작하므로, URL에서 읽은 값에서 1을 뺍니다.
            const page = parseInt(params.get('page')) || 0; 
            
            // fetchPublicMusicList 함수는 0-indexed page를 받으므로 그대로 전달합니다.
            fetchPublicMusicList(page);
        } else {
            // AuthContext가 아직 로딩 중이면 음악 목록 로딩 상태도 true로 유지합니다.
            setIsLoadingPublicMusic(true);
        }
    }, [fetchPublicMusicList, authLoading]);
    // 플레이리스트 생성 모달 제출 핸들러
    const handleCreatePlaylistSubmit = async (event) => {
        event.preventDefault();
        setPlaylistMessage(''); // 메시지 초기화

        if (!playlistTitle.trim()) {
            setPlaylistMessage('플레이리스트 제목은 필수입니다.');
            return;
        }

        try {
            const response = await apiClient.post('/api/playlists', {
                title: playlistTitle,
                description: playlistDescription
            });

            const newPlaylist = response.data;
            showTemporaryMessage(`플레이리스트 '${newPlaylist.title}'이(가) 성공적으로 생성되었습니다.`, true);
            setShowPlaylistModal(false); // 모달 닫기
            setPlaylistTitle(''); // 폼 초기화
            setPlaylistDescription(''); // 폼 초기화
            // 플레이리스트 생성 후 필요하다면 목록 새로고침 등 (예: fetchMyPlaylists())
        } catch (error) {
            console.error('플레이리스트 생성 실패:', error);
            const errorMessage = error.response?.data?.message || '네트워크 오류 또는 서버 응답 문제 발생.';
            setPlaylistMessage(`플레이리스트 생성 실패: ${errorMessage}`);
        }
    };

    // 로그아웃 핸들러
    const handleLogout = async () => {
        try {
            await authLogout(); // AuthContext의 로그아웃 함수 호출 (토큰 삭제, 상태 업데이트, 리다이렉트 포함)
            showTemporaryMessage('로그아웃 되었습니다.', true);
            // AuthContext의 logout 함수가 navigate를 처리하므로 여기서는 중복 호출 방지
        } catch (error) {
            console.error('로그아웃 중 오류 발생:', error);
            showTemporaryMessage('로그아웃 중 오류가 발생했습니다.', false);
        }
    };

    // ⭐ 페이지네이션 버튼 렌더링 함수 수정 ⭐
    const renderPagination = () => {
        const pages = [];
        // totalPages가 0보다 크고 musicList.length가 0이 아니면 페이지네이션 렌더링
        if (pageInfo.totalPages <= 0 && musicList.length === 0) return null; // ⭐ 수정된 조건 ⭐

        // 현재 페이지가 totalPages 이상이면 0으로 리셋 (API 응답 변경에 따른 안전 장치)
        const currentPage = pageInfo.pageNumber >= pageInfo.totalPages && pageInfo.totalPages > 0 ? pageInfo.totalPages - 1 : pageInfo.pageNumber;
        const displayPageStart = Math.max(0, currentPage - 2);
        const displayPageEnd = Math.min(pageInfo.totalPages - 1, currentPage + 2);

        for (let i = displayPageStart; i <= displayPageEnd; i++) {
            pages.push(
                <button
                    key={i}
                    onClick={() => fetchPublicMusicList(i)}
                    className={`px-4 py-2 mx-1 rounded-md ${
                        pageInfo.pageNumber === i ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
                    }`}
                >
                    {i + 1}
                </button>
            );
        }
        return (
            <div className="flex justify-center items-center mt-6 space-x-4">
                <button
                    onClick={() => fetchPublicMusicList(pageInfo.pageNumber - 1)}
                    disabled={pageInfo.first}
                    className="px-4 py-2 mx-1 rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300 disabled:opacity-50"
                >
                    이전
                </button>
                {pages}
                <button
                    onClick={() => fetchPublicMusicList(pageInfo.pageNumber + 1)}
                    disabled={pageInfo.last}
                    className="px-4 py-2 mx-1 rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300 disabled:opacity-50"
                >
                    다음
                </button>
            </div>
        );
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
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-search"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg> 검색
                    </button>
                </div>
            </header>

            {/* Navigation */}
            <nav className="bg-gray-800 text-white py-3 shadow-md">
                <ul className="flex flex-wrap justify-center space-x-4 md:space-x-8 text-lg">
                    {/* 차트100 */}
                    <li className="relative group">
                        <button onClick={() => handleDropdownToggle('chart')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out bg-transparent border-none text-white cursor-pointer"> {/* Changed to button */}
                            차트100
                        </button>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'chart' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/charts/daily">일간 차트</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/charts/weekly">주간 차트</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/charts/monthly">월간 차트</Link></li>
                        </ul>
                    </li>
                    {/* 최신음악 */}
                    <li className="relative group">
                        <button onClick={() => handleDropdownToggle('newMusic')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out bg-transparent border-none text-white cursor-pointer"> {/* Changed to button */}
                            최신음악
                        </button>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'newMusic' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/new-music/songs">최신 곡</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/new-music/albums">최신 앨범</Link></li>
                        </ul>
                    </li>
                    {/* 장르음악 */}
                    <li className="relative group">
                        <button onClick={() => handleDropdownToggle('genre')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out bg-transparent border-none text-white cursor-pointer"> {/* Changed to button */}
                            장르음악
                        </button>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'genre' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/genres/ballad">발라드</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/genres/dance">댄스</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/genres/hiphop">힙합</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/genres/rock">록</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/genres/jazz">재즈</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/genres/pop">POP</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/genres/rnb">R&B</Link></li>
                        </ul>
                    </li>
                    {/* 공지사항 */}
                    <li className="relative group">
                        <button onClick={() => handleDropdownToggle('notice')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out bg-transparent border-none text-white cursor-pointer"> {/* Changed to button */}
                            공지사항
                        </button>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'notice' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/notice-list">일반 공지</Link></li> {/* Corrected to "/notice-list" */}
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/notice/updates">업데이트 소식</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/notice/events">이벤트</Link></li>
                        </ul>
                    </li>
                    {/* 추천 */}
                    <li className="relative group">
                        <button onClick={() => handleDropdownToggle('recommend')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out bg-transparent border-none text-white cursor-pointer"> {/* Changed to button */}
                            추천
                        </button>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'recommend' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/recommend/custom">맞춤 추천</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/recommend/editor">에디터 추천</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/recommend/playlists">인기 플레이리스트</Link></li>
                        </ul>
                    </li>
                     {/* 자유게시판 */}
                    <li className="relative group">
                        <button onClick={() => handleDropdownToggle('freeboard')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out bg-transparent border-none text-white cursor-pointer"> {/* Changed to button */}
                            자유게시판
                        </button>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'freeboard' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/posts">인기 글</Link></li> {/* Corrected to "/posts" */}
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/posts/new">새 글 작성</Link></li> {/* Corrected to "/posts/new" */}
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/posts">내 글 보기</Link></li> {/* Corrected to "/posts" */}
                        </ul>
                    </li>
                    {/* 뮤직허그 */}
                    <li className="relative group">
                        <button onClick={() => handleDropdownToggle('musicHug')} className="nav-link p-2 block hover:text-blue-300 transition duration-150 ease-in-out bg-transparent border-none text-white cursor-pointer"> {/* Changed to button */}
                            뮤직허그
                        </button>
                        <ul className={`absolute left-1/2 -translate-x-1/2 mt-2 w-36 bg-white text-gray-800 rounded-md shadow-lg py-1 z-20 ${openDropdown === 'musicHug' ? 'block' : 'hidden group-hover:block'}`}>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/music-hug/listen">실시간 듣기</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/music-hug/invite">친구 초대</Link></li>
                            <li><Link className="block px-4 py-2 hover:bg-gray-100 rounded-md" to="/music-hug/my-room">내 허그룸</Link></li>
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
                    {isAuthenticated && authUser ? ( // AuthContext의 isAuthenticated와 authUser 사용
                        <>
                            <p className="text-gray-600"><strong>이름:</strong> <span>{authUser.username}</span></p>
                            <p className="text-gray-600"><strong>이메일:</strong> <span>{authUser.email}</span></p>
                            <p className="text-gray-600"><strong>닉네임:</strong> <span>{authUser.nickname}</span></p>
                        </>
                    ) : (
                        <p className="text-gray-600">사용자 정보를 불러올 수 없습니다. 로그인 상태를 확인해주세요.</p>
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
                    {/* ⭐ musicList를 사용하고, 내용이 없을 때 메시지 표시 ⭐ */}
                    {!isLoadingPublicMusic && musicList.length === 0 && (
                        <p className="text-center text-gray-500">등록된 음악이 없습니다.</p>
                    )}
                    {/* ⭐ public-music-list-container의 musicList.content 대신 musicList 직접 사용 ⭐ */}
                    <div id="public-music-list-container" className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                        {musicList.map(music => ( // ⭐ publicMusicList.content 대신 musicList 사용 ⭐
                            <div key={music.id} className="card bg-white rounded-lg shadow-md overflow-hidden transform transition-transform duration-200 hover:scale-105">
                                <Link to={`/music/${music.id}`} className="block">
                                    <img
                                        // ⭐ coverImageUrl이 없거나 오류 발생 시 기본 placeholder 이미지 사용 ⭐
                                        src={music.coverImageUrl || `https://placehold.co/400x400/cccccc/333333?text=No+Image`}
                                        alt="앨범 커버"
                                        onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/400x400/cccccc/333333?text=No+Image"; }}
                                        className="w-full h-48 object-cover"
                                    />
                                </Link>
                                <div className="p-4 flex flex-col justify-between items-start h-auto">
                                    <Link to={`/music/${music.id}`} className="block text-left mb-2 w-full">
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
                                            {currentPlayingMusicId === music.id ?
                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-pause"><rect width="4" height="16" x="6" y="4"/><rect width="4" height="16" x="14" y="4"/></svg> :
                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-play"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                                            }
                                            {currentPlayingMusicId === music.id ? ' 일시정지' : ' 재생'}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                    {/* ⭐ 페이지네이션 버튼에 pageInfo 사용 ⭐ */}
                    <div className="flex justify-center items-center mt-6 space-x-4">
                        <button
                            onClick={() => fetchPublicMusicList(pageInfo.pageNumber - 1)}
                            disabled={pageInfo.first}
                            className="bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition duration-150 ease-in-out"
                        >
                            이전 페이지
                        </button>
                        <span className="text-gray-700 font-medium">페이지 {pageInfo.pageNumber + 1} / {pageInfo.totalPages}</span>
                        <button
                            onClick={() => fetchPublicMusicList(pageInfo.pageNumber + 1)}
                            disabled={pageInfo.last}
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
                                        {currentPlayingMusicId === music.id ?
                                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-pause"><rect width="4" height="16" x="6" y="4"/><rect width="4" height="16" x="14" y="4"/></svg> :
                                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-play"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                                        }
                                        {currentPlayingMusicId === music.id ? ' 일시정지' : ' 재생'}
                                    </button>
                                </div>
                            </div>
                        ))
                    ) : (
                        searchKeyword.trim() ? <p className="text-center text-gray-500">검색 결과가 없습니다.</p> : <p className="text-center text-gray-500">검색어를 입력해 주세요.</p>
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
                    <p className="mt-4"><Link to="/my-page" className="text-blue-600 hover:underline">마이페이지</Link></p> {/* ⭐ 수정: /mypage -> /my-page */}
                </div>

                {isAuthenticated && authUser?.roles?.includes('ROLE_ADMIN') && ( // ⭐ AuthContext의 isAuthenticated와 authUser 사용
                    <div className="mt-4">
                        <Link to="/admin/music" className="text-purple-600 hover:underline font-bold">음악 관리 페이지</Link> {/* ⭐ 수정: /music_upload -> /admin/music */}
                    </div>
                )}

                {isAuthenticated && ( // ⭐ 로그인했을 때만 내 플레이리스트 보기 버튼 표시
                    <div className="mt-4">
                        <Link to="/my-playlists" className="text-purple-600 hover:underline font-bold">내 플레이리스트 보기</Link>
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
