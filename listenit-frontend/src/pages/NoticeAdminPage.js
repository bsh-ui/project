import React, { useState, useEffect } from 'react';

// API 기본 URL 설정
const API_BASE_URL = 'http://localhost:8485'; // 백엔드 주소에 맞게 변경하세요.

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

// JWT 토큰을 쿠키에서 삭제하는 헬퍼 함수
const deleteCookie = (name) => {
    document.cookie = name + '=; Max-Age=-99999999; path=/';
};

function NoticeAdminPage() {
    const [noticeId, setNoticeId] = useState('');
    const [noticeType, setNoticeType] = useState('');
    const [noticeTitle, setNoticeTitle] = useState('');
    const [noticeContent, setNoticeContent] = useState('');
    const [notices, setNotices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState(''); // 사용자에게 표시할 메시지
    const [isSuccessMessage, setIsSuccessMessage] = useState(false); // 메시지 타입 (성공/실패)
    const [jwtToken, setJwtToken] = useState('');
    const [showConfirmModal, setShowConfirmModal] = useState(false); // 확인 모달 표시 여부
    const [confirmAction, setConfirmAction] = useState(null); // 확인 모달에서 실행할 함수
    const [confirmMessage, setConfirmMessage] = useState(''); // 확인 모달 메시지

    const pageSize = 10; // 페이지당 항목 수 (현재는 고정)

    // 컴포넌트 마운트 시 초기 데이터 로드 및 토큰 확인
    useEffect(() => {
        const storedToken = getCookie('jwt_token');
        if (storedToken) {
            setJwtToken(storedToken);
            fetchNoticesForAdmin(storedToken);
        } else {
            setMessage('관리자 권한이 필요합니다. 로그인 페이지로 이동합니다.');
            setIsSuccessMessage(false);
            setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
        }
    }, []);

    // 공지사항 목록 불러오기 (관리자 페이지용)
    const fetchNoticesForAdmin = async (token) => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`${API_BASE_URL}?page=0&size=${pageSize}&sort=createdAt,desc`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    setMessage('권한이 없습니다. 관리자 계정으로 로그인해주세요.');
                    setIsSuccessMessage(false);
                    deleteCookie('jwt_token'); // 유효하지 않은 토큰 삭제
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const pageData = await response.json();
            setNotices(pageData.content);

        } catch (err) {
            console.error('Error fetching notices for admin:', err);
            setError('공지사항을 불러오는 데 실패했습니다.');
            setNotices([]);
        } finally {
            setLoading(false);
        }
    };

    // 폼 초기화 함수
    const resetForm = () => {
        setNoticeId('');
        setNoticeType('');
        setNoticeTitle('');
        setNoticeContent('');
        setMessage('');
    };

    // 공지사항 로드하여 수정 폼에 채우기
    const loadNoticeForEdit = async (id) => {
        try {
            const response = await fetch(`${API_BASE_URL}/${id}`, {
                headers: {
                    'Authorization': `Bearer ${jwtToken}`
                }
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    setMessage('권한이 없습니다. 관리자 계정으로 로그인해주세요.');
                    setIsSuccessMessage(false);
                    deleteCookie('jwt_token');
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const notice = await response.json();

            setNoticeId(notice.id);
            setNoticeType(notice.type);
            setNoticeTitle(notice.title);
            setNoticeContent(notice.content);

            // 폼으로 스크롤 이동
            window.scrollTo({ top: 0, behavior: 'smooth' });

        } catch (err) {
            console.error('Error loading notice for edit:', err);
            setMessage('공지사항 정보를 불러오는 데 실패했습니다.');
            setIsSuccessMessage(false);
        }
    };

    // 공지사항 생성 또는 수정
    const handleSubmit = async (event) => {
        event.preventDefault();

        if (!noticeType || !noticeTitle || !noticeContent) {
            setMessage('모든 필드를 채워주세요.');
            setIsSuccessMessage(false);
            return;
        }

        const noticeData = { type: noticeType, title: noticeTitle, content: noticeContent };

        let response;
        try {
            if (noticeId) { // ID가 있으면 수정 (PUT)
                response = await fetch(`${API_BASE_URL}/${noticeId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwtToken}`
                    },
                    body: JSON.stringify(noticeData)
                });
            } else { // ID가 없으면 생성 (POST)
                response = await fetch(API_BASE_URL, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwtToken}`
                    },
                    body: JSON.stringify(noticeData)
                });
            }

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    setMessage('권한이 없습니다. 관리자 계정으로 로그인해주세요.');
                    setIsSuccessMessage(false);
                    deleteCookie('jwt_token');
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                    return;
                }
                const errorText = await response.text();
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
            }

            const result = await response.json();
            setMessage(noticeId ? '공지사항이 성공적으로 수정되었습니다!' : '새로운 공지사항이 성공적으로 생성되었습니다!');
            setIsSuccessMessage(true);
            resetForm(); // 폼 초기화
            fetchNoticesForAdmin(jwtToken); // 목록 새로고침

        } catch (err) {
            console.error('Error saving notice:', err);
            setMessage(`공지사항 저장에 실패했습니다: ${err.message}`);
            setIsSuccessMessage(false);
        }
    };

    // 공지사항 삭제 확인 모달 트리거
    const confirmDelete = (id) => {
        setConfirmMessage('정말로 이 공지사항을 삭제하시겠습니까?');
        setConfirmAction(() => () => handleDelete(id)); // 클로저로 handleDelete 함수와 id 전달
        setShowConfirmModal(true);
    };

    // 실제 공지사항 삭제 로직
    const handleDelete = async (id) => {
        setShowConfirmModal(false); // 확인 모달 닫기

        try {
            const response = await fetch(`${API_BASE_URL}/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${jwtToken}`
                }
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    setMessage('권한이 없습니다. 관리자 계정으로 로그인해주세요.');
                    setIsSuccessMessage(false);
                    deleteCookie('jwt_token');
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                    return;
                }
                const errorText = await response.text();
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
            }

            setMessage('공지사항이 성공적으로 삭제되었습니다!');
            setIsSuccessMessage(true);
            fetchNoticesForAdmin(jwtToken); // 목록 새로고침
        } catch (err) {
            console.error('Error deleting notice:', err);
            setMessage(`공지사항 삭제에 실패했습니다: ${err.message}`);
            setIsSuccessMessage(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 font-sans py-10">
            {/* Message Display */}
            {message && (
                <div className={`fixed top-4 right-4 z-50 p-4 rounded-md shadow-lg ${isSuccessMessage ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {message}
                </div>
            )}

            <div className="container mx-auto max-w-4xl px-4">
                <h2 className="mb-8 text-3xl font-bold text-center text-gray-800">공지사항 관리</h2>
                <div className="absolute top-6 right-6">
                    <a href="/main" className="btn bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-4 rounded-md transition duration-200">메인으로</a>
                </div>

                <div className="card bg-white p-6 rounded-lg shadow-xl mb-8">
                    <div className="bg-green-600 text-white text-xl font-bold py-3 px-5 rounded-t-lg -mx-6 -mt-6 mb-6">공지사항 생성/수정</div>
                    <div className="card-body">
                        <form onSubmit={handleSubmit}>
                            <input type="hidden" id="notice-id" value={noticeId} />
                            <div className="mb-4">
                                <label htmlFor="notice-type" className="block text-gray-700 text-sm font-bold mb-2">타입</label>
                                <select
                                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    id="notice-type"
                                    value={noticeType}
                                    onChange={(e) => setNoticeType(e.target.value)}
                                    required
                                >
                                    <option value="">선택</option>
                                    <option value="NOTICE">공지</option>
                                    <option value="EVENT">이벤트</option>
                                </select>
                            </div>
                            <div className="mb-4">
                                <label htmlFor="notice-title" className="block text-gray-700 text-sm font-bold mb-2">제목</label>
                                <input
                                    type="text"
                                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    id="notice-title"
                                    value={noticeTitle}
                                    onChange={(e) => setNoticeTitle(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="mb-4">
                                <label htmlFor="notice-content" className="block text-gray-700 text-sm font-bold mb-2">내용</label>
                                <textarea
                                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    id="notice-content"
                                    rows="8"
                                    value={noticeContent}
                                    onChange={(e) => setNoticeContent(e.target.value)}
                                    required
                                ></textarea>
                            </div>
                            <div className="flex justify-end gap-3 mt-6">
                                <button
                                    type="submit"
                                    className={`py-2 px-5 rounded-md font-semibold transition duration-200 ${noticeId ? 'bg-yellow-500 hover:bg-yellow-600 text-white' : 'bg-green-600 hover:bg-green-700 text-white'}`}
                                >
                                    {noticeId ? '수정' : '생성'}
                                </button>
                                <button
                                    type="button"
                                    className="py-2 px-5 rounded-md font-semibold bg-gray-500 hover:bg-gray-600 text-white transition duration-200"
                                    onClick={resetForm}
                                >
                                    초기화
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <div className="card notice-list-card bg-white p-6 rounded-lg shadow-xl">
                    <div className="bg-blue-600 text-white text-xl font-bold py-3 px-5 rounded-t-lg -mx-6 -mt-6 mb-6">기존 공지사항 목록</div>
                    <div className="card-body" id="admin-notice-list">
                        {loading ? (
                            <p className="text-center text-gray-500 py-10">공지사항을 불러오는 중...</p>
                        ) : error ? (
                            <p className="text-center text-red-500 py-10">{error}</p>
                        ) : notices.length === 0 ? (
                            <p className="text-center text-gray-500 py-10">등록된 공지사항이 없습니다.</p>
                        ) : (
                            notices.map(notice => (
                                <div key={notice.id} className="flex justify-between items-center py-3 border-b border-gray-200 last:border-b-0">
                                    <span className="flex-grow text-gray-800 text-base font-semibold">
                                        {notice.title}
                                        <small className="text-gray-500 ml-2 text-sm">({notice.type === 'NOTICE' ? '공지' : '이벤트'})</small>
                                    </span>
                                    <div className="flex items-center gap-2">
                                        <button
                                            className="py-1 px-3 bg-yellow-500 hover:bg-yellow-600 text-white text-sm rounded-md transition duration-200"
                                            onClick={() => loadNoticeForEdit(notice.id)}
                                        >
                                            수정
                                        </button>
                                        <button
                                            className="py-1 px-3 bg-red-600 hover:bg-red-700 text-white text-sm rounded-md transition duration-200"
                                            onClick={() => confirmDelete(notice.id)}
                                        >
                                            삭제
                                        </button>
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

export default NoticeAdminPage;
