import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
// Bootstrap CSS는 Tailwind와 충돌할 수 있으므로 제거하거나 필요한 부분만 사용합니다.
// Bootstrap Icons 대신 lucide-react를 사용하도록 변경합니다.
// import 'bootstrap-icons/font/bootstrap-icons.css';
import { PlusCircle, ListMusic, Pencil, Trash, Play } from 'lucide-react'; // lucide-react 아이콘 임포트

// API 기본 URL 설정
const API_BASE_URL = '/api/playlists';
const MUSIC_API_BASE_URL = 'http://localhost:8485/api/music'; // 음악 파일 스트리밍/커버 이미지 경로를 위해 추가

// JWT 토큰 가져오는 함수 (실제 구현에 따라 달라질 수 있음)
function getJwtToken() {
    return localStorage.getItem('jwtToken'); // 또는 sessionStorage 등
}

// duration (초)을 "분:초" 형식으로 포맷팅하는 헬퍼 함수
function formatDuration(seconds) {
    if (typeof seconds !== 'number' || isNaN(seconds) || seconds < 0) {
        return '0:00';
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60);
    return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
}

function MyPlaylists() {
    const [playlists, setPlaylists] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    // 전역 메시지 상태 (alert 대체)
    const [globalMessage, setGlobalMessage] = useState('');
    const [isSuccessGlobalMessage, setIsSuccessGlobalMessage] = useState(false);
    const messageTimeoutRef = useRef(null);

    // 플레이리스트 생성 모달 관련 상태
    const [showCreatePlaylistModal, setShowCreatePlaylistModal] = useState(false);
    const [newPlaylistTitle, setNewPlaylistTitle] = useState('');
    const [newPlaylistDescription, setNewPlaylistDescription] = useState('');
    const [createPlaylistMessage, setCreatePlaylistMessage] = useState(''); // 생성 모달 내 메시지

    // 플레이리스트 수정 모달 관련 상태
    const [showEditPlaylistModal, setShowEditPlaylistModal] = useState(false);
    const [editPlaylistId, setEditPlaylistId] = useState(null);
    const [editPlaylistTitle, setEditPlaylistTitle] = useState('');
    const [editPlaylistDescription, setEditPlaylistDescription] = useState('');
    const [editPlaylistIsPublic, setEditPlaylistIsPublic] = useState(false);
    const [editPlaylistMessage, setEditPlaylistMessage] = useState(''); // 수정 모달 내 메시지

    // 플레이리스트 상세 보기 모달 관련 상태
    const [showViewPlaylistModal, setShowViewPlaylistModal] = useState(false);
    const [viewingPlaylist, setViewingPlaylist] = useState(null);
    const [musicIdToAdd, setMusicIdToAdd] = useState('');
    const [viewPlaylistMessage, setViewPlaylistMessage] = useState(''); // 상세 보기 모달 내 메시지

    // 확인/경고 모달 (window.confirm 대체)
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [confirmMessage, setConfirmMessage] = useState('');
    const [confirmAction, setConfirmAction] = useState(null); // 실행할 함수

    // 전역 메시지 표시 함수
    const showTemporaryMessage = (msg, isSuccess = false) => {
        if (messageTimeoutRef.current) {
            clearTimeout(messageTimeoutRef.current);
        }
        setGlobalMessage(msg);
        setIsSuccessGlobalMessage(isSuccess);
        messageTimeoutRef.current = setTimeout(() => {
            setGlobalMessage('');
        }, 3000);
    };

    // fetch 요청 시 JWT 토큰 포함하고 401/403 처리하는 헬퍼 함수
    const authenticatedFetch = async (url, options = {}) => {
        const token = getJwtToken();
        if (token) {
            options.headers = {
                ...options.headers,
                'Authorization': `Bearer ${token}`
            };
        }
        const response = await fetch(url, options);
        if (response.status === 401 || response.status === 403) {
            showTemporaryMessage('로그인이 필요하거나 권한이 없습니다. 로그인 페이지로 이동합니다.', false);
            navigate('/custom_login');
            throw new Error('Unauthorized or Forbidden');
        }
        return response;
    };

    // 내 플레이리스트 목록을 불러오는 함수
    const fetchMyPlaylists = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await authenticatedFetch(`${API_BASE_URL}/my`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            setPlaylists(data);
        } catch (err) {
            console.error('Error fetching playlists:', err);
            if (err.message !== 'Unauthorized or Forbidden') { // 이미 authenticatedFetch에서 처리된 에러는 무시
                setError('플레이리스트 목록을 불러오는 데 실패했습니다.');
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMyPlaylists();
        // 컴포넌트 언마운트 시 메시지 타임아웃 클리어
        return () => {
            if (messageTimeoutRef.current) {
                clearTimeout(messageTimeoutRef.current);
            }
        };
    }, []);

    // 플레이리스트 생성 핸들러
    const handleCreatePlaylist = async (event) => {
        event.preventDefault();
        setCreatePlaylistMessage(''); // 메시지 초기화
        if (!newPlaylistTitle.trim()) {
            setCreatePlaylistMessage('플레이리스트 제목은 필수입니다.');
            return;
        }

        try {
            const response = await authenticatedFetch(API_BASE_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title: newPlaylistTitle, description: newPlaylistDescription })
            });

            if (response.ok) {
                const newPlaylist = await response.json();
                showTemporaryMessage(`플레이리스트 '${newPlaylist.title}'이(가) 생성되었습니다.`, true);
                setShowCreatePlaylistModal(false); // 모달 닫기
                setNewPlaylistTitle('');
                setNewPlaylistDescription('');
                fetchMyPlaylists(); // 목록 새로고침
            } else if (response.status === 400) {
                const errorData = await response.json();
                setCreatePlaylistMessage('플레이리스트 생성 실패: ' + (errorData.message || '제목을 확인해주세요.'));
            } else {
                setCreatePlaylistMessage('플레이리스트 생성 중 오류가 발생했습니다.');
                console.error('플레이리스트 생성 실패:', response.status, await response.text());
            }
        } catch (err) {
            console.error('Create playlist error:', err);
            if (err.message !== 'Unauthorized or Forbidden') {
                setCreatePlaylistMessage('네트워크 오류 또는 서버 응답 문제 발생.');
            }
        }
    };

    // 플레이리스트 수정 핸들러
    const handleEditPlaylist = async (event) => {
        event.preventDefault();
        setEditPlaylistMessage(''); // 메시지 초기화
        if (!editPlaylistTitle.trim()) {
            setEditPlaylistMessage('플레이리스트 제목은 필수입니다.');
            return;
        }

        try {
            const response = await authenticatedFetch(`${API_BASE_URL}/${editPlaylistId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    title: editPlaylistTitle,
                    description: editPlaylistDescription,
                    isPublic: editPlaylistIsPublic
                })
            });

            if (response.ok) {
                const updatedPlaylist = await response.json();
                showTemporaryMessage(`플레이리스트 '${updatedPlaylist.title}'이(가) 성공적으로 수정되었습니다.`, true);
                setShowEditPlaylistModal(false); // 모달 닫기
                fetchMyPlaylists();
            } else if (response.status === 403) {
                setEditPlaylistMessage('이 플레이리스트를 수정할 권한이 없습니다.');
            } else if (response.status === 404) {
                setEditPlaylistMessage('수정하려는 플레이리스트를 찾을 수 없습니다.');
            } else if (response.status === 400) {
                const errorData = await response.json();
                setEditPlaylistMessage('플레이리스트 수정 실패: ' + (errorData.message || '제목을 확인해주세요.'));
            } else {
                setEditPlaylistMessage('플레이리스트 수정 중 오류가 발생했습니다.');
                console.error('Edit playlist failed:', response.status, await response.text());
            }
        } catch (err) {
            console.error('Edit playlist error:', err);
            if (err.message !== 'Unauthorized or Forbidden') {
                setEditPlaylistMessage('네트워크 오류 또는 서버 응답 문제 발생.');
            }
        }
    };

    // 플레이리스트 삭제 핸들러 (확인 모달 트리거)
    const confirmDeletePlaylist = (playlistId) => {
        setConfirmMessage('정말로 이 플레이리스트를 삭제하시겠습니까?');
        setConfirmAction(() => () => handleDeletePlaylist(playlistId)); // 클로저로 playlistId 전달
        setShowConfirmModal(true);
    };

    // 실제 플레이리스트 삭제 로직
    const handleDeletePlaylist = async (playlistId) => {
        setShowConfirmModal(false); // 확인 모달 닫기
        try {
            const response = await authenticatedFetch(`${API_BASE_URL}/${playlistId}`, {
                method: 'DELETE'
            });

            if (response.status === 204) { // 204 No Content
                showTemporaryMessage('플레이리스트가 성공적으로 삭제되었습니다.', true);
                fetchMyPlaylists(); // 목록 새로고침
            } else if (response.status === 403) {
                showTemporaryMessage('이 플레이리스트를 삭제할 권한이 없습니다.', false);
            } else if (response.status === 404) {
                showTemporaryMessage('삭제하려는 플레이리스트를 찾을 수 없습니다.', false);
            } else {
                const errorData = await response.json();
                showTemporaryMessage('플레이리스트 삭제 실패: ' + (errorData.message || '알 수 없는 오류'), false);
                console.error('Delete playlist failed:', response.status, await response.text());
            }
        } catch (err) {
            console.error('Delete playlist error:', err);
            if (err.message !== 'Unauthorized or Forbidden') {
                showTemporaryMessage('네트워크 오류 또는 서버 응답 문제 발생.', false);
            }
        }
    };

    // 플레이리스트 상세 정보 로드 및 모달 표시 함수
    const loadPlaylistDetail = async (playlistId) => {
        setViewingPlaylist(null); // 이전 데이터 초기화
        setMusicIdToAdd(''); // 음악 추가 input 초기화
        setViewPlaylistMessage(''); // 메시지 초기화
        setShowViewPlaylistModal(true); // 모달 열기

        try {
            const response = await authenticatedFetch(`${API_BASE_URL}/${playlistId}`);
            if (response.ok) {
                const playlist = await response.json();
                setViewingPlaylist(playlist);
            } else if (response.status === 404) {
                showTemporaryMessage('플레이리스트를 찾을 수 없습니다.', false);
                setShowViewPlaylistModal(false); // 모달 닫기
            } else {
                showTemporaryMessage('플레이리스트 상세 정보를 불러오는 데 실패했습니다.', false);
                console.error('Load playlist detail failed:', response.status, await response.text());
                setShowViewPlaylistModal(false); // 모달 닫기
            }
        } catch (err) {
            console.error('Load playlist detail error:', err);
            if (err.message !== 'Unauthorized or Forbidden') {
                showTemporaryMessage('네트워크 오류 또는 서버 응답 문제 발생.', false);
            }
            setShowViewPlaylistModal(false); // 모달 닫기
        }
    };

    // 플레이리스트에 음악 추가 (확인 모달 트리거)
    const handleAddMusicToPlaylist = async () => {
        if (!viewingPlaylist || !viewingPlaylist.id) {
            setViewPlaylistMessage('플레이리스트를 먼저 선택해주세요.');
            return;
        }
        if (!musicIdToAdd || isNaN(musicIdToAdd) || parseInt(musicIdToAdd) <= 0) {
            setViewPlaylistMessage('유효한 음악 ID를 입력해주세요.');
            return;
        }

        try {
            const response = await authenticatedFetch(`${API_BASE_URL}/${viewingPlaylist.id}/music/${musicIdToAdd}`, {
                method: 'POST'
            });

            if (response.ok) {
                showTemporaryMessage('음악이 플레이리스트에 성공적으로 추가되었습니다!', true);
                setMusicIdToAdd(''); // 입력 필드 초기화
                loadPlaylistDetail(viewingPlaylist.id); // 상세 정보 새로고침
                fetchMyPlaylists(); // 플레이리스트 카드 업데이트 (음악 수 등)
                setViewPlaylistMessage(''); // 성공 시 모달 내 메시지 초기화
            } else if (response.status === 404) {
                setViewPlaylistMessage('플레이리스트 또는 음악을 찾을 수 없습니다.');
            } else if (response.status === 409) {
                setViewPlaylistMessage('이미 플레이리스트에 있는 음악입니다.');
            } else {
                const errorData = await response.json();
                setViewPlaylistMessage('음악 추가 실패: ' + (errorData.message || '알 수 없는 오류'));
                console.error('Add music to playlist failed:', response.status, await response.text());
            }
        } catch (err) {
            console.error('Add music to playlist error:', err);
            if (err.message !== 'Unauthorized or Forbidden') {
                setViewPlaylistMessage('네트워크 오류 또는 서버 응답 문제 발생.');
            }
        }
    };

    // 플레이리스트에서 음악 삭제 (확인 모달 트리거)
    const confirmRemoveMusicFromPlaylist = (playlistId, musicId, musicTitle) => {
        setConfirmMessage(`'${musicTitle}'을(를) 플레이리스트에서 삭제하시겠습니까?`);
        setConfirmAction(() => () => handleRemoveMusicFromPlaylist(playlistId, musicId));
        setShowConfirmModal(true);
    };

    // 실제 플레이리스트에서 음악 삭제 로직
    const handleRemoveMusicFromPlaylist = async (playlistId, musicId) => {
        setShowConfirmModal(false); // 확인 모달 닫기
        try {
            const response = await authenticatedFetch(`${API_BASE_URL}/${playlistId}/music/${musicId}`, {
                method: 'DELETE'
            });

            if (response.status === 204) {
                showTemporaryMessage('음악이 플레이리스트에서 성공적으로 삭제되었습니다.', true);
                loadPlaylistDetail(playlistId); // 상세 정보 새로고침
                fetchMyPlaylists(); // 플레이리스트 카드 업데이트를 위해
                setViewPlaylistMessage(''); // 성공 시 모달 내 메시지 초기화
            } else if (response.status === 404) {
                showTemporaryMessage('플레이리스트 또는 음악을 찾을 수 없습니다.', false);
            } else if (response.status === 403) {
                showTemporaryMessage('이 음악을 삭제할 권한이 없습니다.', false);
            } else {
                const errorData = await response.json();
                showTemporaryMessage('음악 삭제 실패: ' + (errorData.message || '알 수 없는 오류'), false);
                console.error('Remove music from playlist failed:', response.status, await response.text());
            }
        } catch (err) {
            console.error('Remove music from playlist error:', err);
            if (err.message !== 'Unauthorized or Forbidden') {
                showTemporaryMessage('네트워크 오류 또는 서버 응답 문제 발생.', false);
            }
        }
    };

    // 플레이리스트 수정 모달 열릴 때 데이터 설정
    const openEditModal = (playlist) => {
        setEditPlaylistId(playlist.id);
        setEditPlaylistTitle(playlist.title);
        setEditPlaylistDescription(playlist.description || '');
        setEditPlaylistIsPublic(playlist.isPublic);
        setEditPlaylistMessage(''); // 메시지 초기화
        setShowEditPlaylistModal(true);
    };

    return (
        <div className="font-sans bg-gray-100 text-gray-800 min-h-screen pb-10">
            {/* Global Message Display */}
            {globalMessage && (
                <div className={`fixed top-4 right-4 z-50 p-4 rounded-md shadow-lg ${isSuccessGlobalMessage ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {globalMessage}
                </div>
            )}

            <div className="container max-w-4xl mx-auto mt-12 bg-white p-8 rounded-lg shadow-xl">
                <h2 className="text-3xl font-bold mb-4 text-gray-900">내 플레이리스트</h2>

                <div className="flex justify-end gap-3 mb-6">
                    <button
                        type="button"
                        className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md transition duration-150 ease-in-out flex items-center"
                        onClick={() => setShowCreatePlaylistModal(true)}
                    >
                        <PlusCircle className="mr-2 w-5 h-5" /> 새 플레이리스트 생성
                    </button>
                    <Link to="/" className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded-md transition duration-150 ease-in-out">
                        뒤로가기
                    </Link>
                </div>

                <div className="card bg-white rounded-lg shadow-md">
                    <div className="card-header bg-gray-800 text-white text-xl font-bold py-3 px-4 rounded-t-lg">나의 플레이리스트 목록</div>
                    <div className="card-body p-4">
                        {loading && <p className="text-center text-gray-500 py-10">플레이리스트를 불러오는 중...</p>}
                        {error && <p className="text-danger text-center py-10">{error}</p>}
                        {!loading && !error && playlists.length === 0 && (
                            <p className="text-center text-gray-500 py-10">생성된 플레이리스트가 없습니다.</p>
                        )}

                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6" id="my-playlist-list-container">
                            {!loading && !error && playlists.map(playlist => (
                                <div className="col-md-4 col-sm-6" key={playlist.id}>
                                    <div className="bg-white rounded-lg shadow-md overflow-hidden transform transition-transform duration-200 hover:scale-105 playlist-card">
                                        <div className="p-4 flex flex-col h-full">
                                            <h5 className="text-lg font-semibold text-gray-900 mb-2 playlist-title">{playlist.title}</h5>
                                            <p className="text-sm text-gray-600 mb-3 flex-grow playlist-description">{playlist.description || '설명 없음'}</p>
                                            <div className="text-xs text-gray-500 mt-auto playlist-meta">
                                                <p>생성자: {playlist.userNickname}</p>
                                                <p>생성일: {new Date(playlist.createdAt).toLocaleDateString()}</p>
                                                <p>공개여부:
                                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ml-1 ${playlist.isPublic ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                                                        {playlist.isPublic ? '공개' : '비공개'}
                                                    </span>
                                                </p>
                                            </div>
                                            <div className="flex justify-between gap-2 mt-4 action-buttons">
                                                <button
                                                    className="flex-1 bg-indigo-500 hover:bg-indigo-600 text-white font-bold py-2 px-3 rounded-md text-sm transition duration-150 ease-in-out flex items-center justify-center"
                                                    onClick={() => loadPlaylistDetail(playlist.id)}
                                                >
                                                    <ListMusic className="mr-1 w-4 h-4" /> 음악 보기
                                                </button>
                                                <button
                                                    className="flex-1 bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-2 px-3 rounded-md text-sm transition duration-150 ease-in-out flex items-center justify-center"
                                                    onClick={() => openEditModal(playlist)}
                                                >
                                                    <Pencil className="mr-1 w-4 h-4" /> 수정
                                                </button>
                                                <button
                                                    className="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-3 rounded-md text-sm transition duration-150 ease-in-out flex items-center justify-center"
                                                    onClick={() => confirmDeletePlaylist(playlist.id)}
                                                >
                                                    <Trash className="mr-1 w-4 h-4" /> 삭제
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>

            {/* 새 플레이리스트 생성 모달 */}
            {showCreatePlaylistModal && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md mx-auto relative">
                        <button
                            type="button"
                            className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 text-2xl"
                            onClick={() => setShowCreatePlaylistModal(false)}
                        >
                            &times;
                        </button>
                        <h5 className="text-xl font-bold text-gray-800 mb-5 text-center">새 플레이리스트 생성</h5>
                        <Link to="/profile" className="absolute top-3 left-3"> {/* 위치 조정 */}
                            <button type="button" className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-1 px-3 rounded-md text-sm transition duration-150 ease-in-out">
                                내 프로필
                            </button>
                        </Link>
                        <div className="modal-body">
                            <form onSubmit={handleCreatePlaylist}>
                                <div className="mb-4">
                                    <label htmlFor="playlistTitle" className="block text-gray-700 text-sm font-bold mb-2 text-left">플레이리스트 제목</label>
                                    <input
                                        type="text"
                                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                                        id="playlistTitle"
                                        required
                                        maxLength="100"
                                        value={newPlaylistTitle}
                                        onChange={(e) => setNewPlaylistTitle(e.target.value)}
                                    />
                                </div>
                                <div className="mb-6">
                                    <label htmlFor="playlistDescription" className="block text-gray-700 text-sm font-bold mb-2 text-left">설명 (선택 사항)</label>
                                    <textarea
                                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                                        id="playlistDescription"
                                        rows="3"
                                        maxLength="500"
                                        value={newPlaylistDescription}
                                        onChange={(e) => setNewPlaylistDescription(e.target.value)}
                                    ></textarea>
                                </div>
                                {createPlaylistMessage && (
                                    <div className="mb-4 p-3 rounded-md text-center bg-yellow-100 text-yellow-700 font-medium">
                                        {createPlaylistMessage}
                                    </div>
                                )}
                                <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transition duration-150 ease-in-out w-full">
                                    생성하기
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            )}

            {/* 플레이리스트 수정 모달 */}
            {showEditPlaylistModal && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md mx-auto relative">
                        <button
                            type="button"
                            className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 text-2xl"
                            onClick={() => setShowEditPlaylistModal(false)}
                        >
                            &times;
                        </button>
                        <h5 className="text-xl font-bold text-gray-800 mb-5 text-center">플레이리스트 수정</h5>
                        <div className="modal-body">
                            <form onSubmit={handleEditPlaylist}>
                                <input type="hidden" value={editPlaylistId || ''} />
                                <div className="mb-4">
                                    <label htmlFor="editPlaylistTitle" className="block text-gray-700 text-sm font-bold mb-2 text-left">제목</label>
                                    <input
                                        type="text"
                                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                                        id="editPlaylistTitle"
                                        required
                                        maxLength="100"
                                        value={editPlaylistTitle}
                                        onChange={(e) => setEditPlaylistTitle(e.target.value)}
                                    />
                                </div>
                                <div className="mb-6">
                                    <label htmlFor="editPlaylistDescription" className="block text-gray-700 text-sm font-bold mb-2 text-left">설명</label>
                                    <textarea
                                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                                        id="editPlaylistDescription"
                                        rows="3"
                                        maxLength="500"
                                        value={editPlaylistDescription}
                                        onChange={(e) => setEditPlaylistDescription(e.target.value)}
                                    ></textarea>
                                </div>
                                <div className="flex items-center mb-4">
                                    <input
                                        className="form-checkbox h-5 w-5 text-blue-600 rounded"
                                        type="checkbox"
                                        id="editPlaylistIsPublic"
                                        checked={editPlaylistIsPublic}
                                        onChange={(e) => setEditPlaylistIsPublic(e.target.checked)}
                                    />
                                    <label className="ml-2 text-gray-700 text-sm" htmlFor="editPlaylistIsPublic">
                                        공개 플레이리스트
                                    </label>
                                </div>
                                {editPlaylistMessage && (
                                    <div className="mb-4 p-3 rounded-md text-center bg-yellow-100 text-yellow-700 font-medium">
                                        {editPlaylistMessage}
                                    </div>
                                )}
                                <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transition duration-150 ease-in-out w-full">
                                    수정 완료
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            )}

            {/* 플레이리스트 상세 보기 모달 */}
            {showViewPlaylistModal && viewingPlaylist && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-2xl mx-auto relative">
                        <button
                            type="button"
                            className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 text-2xl"
                            onClick={() => setShowViewPlaylistModal(false)}
                        >
                            &times;
                        </button>
                        <h5 className="text-xl font-bold text-gray-800 mb-5 text-center">플레이리스트 상세</h5>
                        <div className="modal-body">
                            <input type="hidden" value={viewingPlaylist.id || ''} />
                            <h4 className="text-2xl font-bold mb-2 text-gray-900">{viewingPlaylist.title}</h4>
                            <p className="text-gray-600 mb-4">{viewingPlaylist.description || '설명 없음'}</p>
                            <hr className="my-4 border-gray-200" />
                            <h5 className="text-lg font-bold text-gray-800 mb-3">포함된 음악</h5>
                            <div className="max-h-80 overflow-y-auto border border-gray-200 rounded-md mb-4">
                                {viewingPlaylist.musics && viewingPlaylist.musics.length > 0 ? (
                                    viewingPlaylist.musics.map(music => (
                                        <div className="flex items-center p-3 border-b border-gray-100 last:border-b-0" key={music.id}>
                                            {music.coverImagePath ? (
                                                <img
                                                    src={`http://localhost:8485/api/files/cover-image/${music.coverImagePath.split('/').pop().split('\\').pop()}`}
                                                    className="w-12 h-12 object-cover rounded-md mr-3 shadow-sm"
                                                    alt="앨범 커버"
                                                    onError={(e) => { e.target.onerror = null; e.target.src = '/images/default_album_cover.png'; }}
                                                />
                                            ) : (
                                                <img
                                                    src="/images/default_album_cover.png"
                                                    className="w-12 h-12 object-cover rounded-md mr-3 shadow-sm"
                                                    alt="기본 앨범 커버"
                                                />
                                            )}
                                            <div className="flex-grow">
                                                <div className="text-base font-semibold text-gray-800">{music.title}</div>
                                                <div className="text-sm text-gray-600">{Array.isArray(music.artist) ? music.artist.join(', ') : music.artist}</div>
                                            </div>
                                            <div className="flex items-center gap-2 ml-4">
                                                {music.duration && (
                                                    <span className="text-sm text-gray-500">{formatDuration(music.duration)}</span>
                                                )}
                                                {/* 음악 재생 버튼 */}
                                                <button
                                                    className="bg-blue-500 hover:bg-blue-600 text-white p-1.5 rounded-full shadow-md transition duration-150 ease-in-out"
                                                    onClick={() => { /* TODO: 실제 음악 재생 로직 연동 */ alert(`음악 재생: ${music.title}`); }}
                                                >
                                                    <Play className="w-5 h-5" />
                                                </button>
                                                {/* 음악 삭제 버튼 */}
                                                <button
                                                    className="bg-red-500 hover:bg-red-600 text-white p-1.5 rounded-full shadow-md transition duration-150 ease-in-out"
                                                    onClick={() => confirmRemoveMusicFromPlaylist(viewingPlaylist.id, music.id, music.title)}
                                                >
                                                    <Trash className="w-5 h-5" />
                                                </button>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <p className="text-center text-gray-500 py-4">이 플레이리스트에 음악이 없습니다.</p>
                                )}
                            </div>

                            <div className="mt-6 p-4 border border-gray-200 rounded-md bg-gray-50">
                                <h6 className="text-md font-bold text-gray-700 mb-3">음악 추가</h6>
                                <div className="flex gap-2">
                                    <input
                                        type="number"
                                        className="flex-grow shadow appearance-none border rounded py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                                        placeholder="추가할 음악 ID 입력"
                                        value={musicIdToAdd}
                                        onChange={(e) => setMusicIdToAdd(e.target.value)}
                                    />
                                    <button
                                        className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transition duration-150 ease-in-out"
                                        onClick={handleAddMusicToPlaylist}
                                    >
                                        음악 추가
                                    </button>
                                </div>
                                <small className="block text-gray-500 mt-2">음악 ID는 검색 페이지 등에서 확인할 수 있습니다.</small>
                                {viewPlaylistMessage && (
                                    <div className="mt-3 p-2 rounded-md text-center bg-yellow-100 text-yellow-700 font-medium">
                                        {viewPlaylistMessage}
                                    </div>
                                )}
                            </div>

                        </div>
                        <div className="modal-footer flex justify-end mt-6">
                            <button
                                type="button"
                                className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={() => setShowViewPlaylistModal(false)}
                            >
                                닫기
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Confirmation Modal (Custom) */}
            {showConfirmModal && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-sm mx-auto relative text-center">
                        <h5 className="text-xl font-bold text-gray-800 mb-5">확인</h5>
                        <p className="text-gray-600 mb-6">{confirmMessage}</p>
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

export default MyPlaylists;
