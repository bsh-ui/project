import React, { useState, useEffect } from 'react';

// API 기본 URL 설정
const API_BASE_URL = 'http://localhost:8485'; // 백엔드 주소에 맞게 변경하세요.

// JWT 토큰을 쿠키에서 가져오는 헬퍼 함수 (필요한 경우 사용)
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

function NoticeListPage() {
    const [notices, setNotices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0); // 백엔드는 0부터 시작
    const [totalPages, setTotalPages] = useState(0);
    const [currentType, setCurrentType] = useState(''); // '', 'NOTICE', 'EVENT'
    const [isAdmin, setIsAdmin] = useState(false); // 관리자 권한 여부

    const pageSize = 10; // 페이지당 항목 수

    // JWT 토큰 확인 및 관리자 권한 확인 (실제 앱에서는 백엔드 API 호출 필요)
    useEffect(() => {
        const token = getCookie('jwt_token');
        if (token) {
            // 이 부분은 실제 백엔드에서 사용자 권한을 확인하는 API 호출로 대체되어야 합니다.
            // 예: fetch(`${API_BASE_URL}/api/user/me`).then(res => res.json()).then(data => setIsAdmin(data.roles.includes('ROLE_ADMIN')));
            // 현재는 토큰이 존재하면 임시로 isAdmin을 true로 설정합니다.
            // 실제 구현에서는 서버에서 사용자 권한을 정확히 확인해야 합니다.
            // fetchUserRole(token); // 예시: 사용자 역할 가져오는 함수 호출
            setIsAdmin(true); // 개발 목적으로 임시 설정, 실제 환경에서는 백엔드 로직 필요
        }
    }, []);

    // 공지사항/이벤트 목록을 가져오는 비동기 함수
    useEffect(() => {
        const fetchNotices = async () => {
            setLoading(true);
            setError(null);

            try {
                let url = `${API_BASE_URL}/api/notices?page=${currentPage}&size=${pageSize}&sort=createdAt,desc`;
                if (currentType) {
                    url += `&type=${currentType}`;
                }

                const response = await fetch(url);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const pageData = await response.json(); // 백엔드에서 Page<NoticeDto> 객체로 반환

                setNotices(pageData.content);
                setTotalPages(pageData.totalPages);

            } catch (err) {
                console.error('Error fetching notices:', err);
                setError('공지사항을 불러오는 데 실패했습니다.');
                setNotices([]);
                setTotalPages(0);
            } finally {
                setLoading(false);
            }
        };

        fetchNotices();
    }, [currentPage, currentType]); // 페이지 또는 타입이 변경될 때마다 재실행

    // 필터링 버튼 클릭 핸들러
    const handleFilterClick = (type) => {
        setCurrentType(type);
        setCurrentPage(0); // 필터링 시 첫 페이지부터 다시 로드
    };

    // 페이지 번호 버튼 클릭 핸들러
    const handlePageClick = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    // 페이지네이션 번호 렌더링
    const renderPageNumbers = () => {
        const pageNumbers = [];
        const maxPageButtons = 5; // 화면에 표시할 최대 페이지 버튼 수
        let startPage = Math.max(0, currentPage - Math.floor(maxPageButtons / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxPageButtons - 1);

        // 끝 페이지가 부족할 경우 시작 페이지 조정
        if (endPage - startPage + 1 < maxPageButtons) {
            startPage = Math.max(0, endPage - maxPageButtons + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            pageNumbers.push(
                <button
                    key={i}
                    className={`px-4 py-2 mx-1 rounded-md transition duration-200 ${i === currentPage ? 'bg-blue-600 text-white font-bold' : 'bg-white text-blue-600 border border-blue-600 hover:bg-blue-50'}`}
                    onClick={() => handlePageClick(i)}
                >
                    {i + 1}
                </button>
            );
        }
        return pageNumbers;
    };

    return (
        <div className="min-h-screen bg-gray-100 font-sans py-10">
            <div className="container mx-auto max-w-4xl px-4">
                <div className="bg-white rounded-lg shadow-xl">
                    <div className="bg-blue-600 text-white text-2xl font-bold py-5 px-6 rounded-t-lg">
                        공지사항 & 이벤트
                    </div>
                    <div className="p-6">
                        <div className="flex flex-wrap gap-3 mb-5 justify-between items-center">
                            <div className="flex flex-wrap gap-2">
                                <button
                                    className={`px-4 py-2 rounded-md border transition duration-200 ${currentType === '' ? 'bg-blue-600 text-white' : 'bg-white text-blue-600 border-blue-600 hover:bg-blue-50'}`}
                                    onClick={() => handleFilterClick('')}
                                >
                                    전체
                                </button>
                                <button
                                    className={`px-4 py-2 rounded-md border transition duration-200 ${currentType === 'NOTICE' ? 'bg-blue-600 text-white' : 'bg-white text-blue-600 border-blue-600 hover:bg-blue-50'}`}
                                    onClick={() => handleFilterClick('NOTICE')}
                                >
                                    공지사항
                                </button>
                                <button
                                    className={`px-4 py-2 rounded-md border transition duration-200 ${currentType === 'EVENT' ? 'bg-blue-600 text-white' : 'bg-white text-blue-600 border-blue-600 hover:bg-blue-50'}`}
                                    onClick={() => handleFilterClick('EVENT')}
                                >
                                    이벤트
                                </button>
                            </div>
                            <div className="flex gap-2">
                                {isAdmin && ( // 관리자 권한이 있을 때만 표시
                                    <a href="/notice_admin" className="px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-md transition duration-200">
                                        관리자 페이지
                                    </a>
                                )}
                                <a href="/main" className="px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-md transition duration-200">
                                    메인으로
                                </a>
                            </div>
                        </div>
                        
                        <div id="notice-list">
                            {loading ? (
                                <p className="text-center text-gray-500 py-10">데이터를 불러오는 중...</p>
                            ) : error ? (
                                <p className="text-center text-red-500 py-10">{error}</p>
                            ) : notices.length === 0 ? (
                                <p className="text-center text-gray-500 py-10">등록된 공지사항이 없습니다.</p>
                            ) : (
                                notices.map(notice => (
                                    <div key={notice.id} className="py-4 border-b border-gray-200 last:border-b-0">
                                        <a href={`/notice_detail?id=${notice.id}`} className="text-lg font-semibold text-gray-800 hover:text-blue-600 transition duration-200">
                                            {notice.title}
                                        </a>
                                        <div className="text-sm text-gray-500 mt-1 flex flex-wrap gap-x-3">
                                            <span>{notice.type === 'NOTICE' ? '공지' : '이벤트'}</span> |
                                            <span>{notice.authorUsername}</span> |
                                            <span>조회수 {notice.viewCount}</span> |
                                            <span>{new Date(notice.createdAt).toLocaleDateString('ko-KR')}</span>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>

                        <nav className="flex justify-center mt-8" aria-label="Page navigation">
                            <button
                                className="px-4 py-2 mx-1 rounded-md border border-blue-600 bg-white text-blue-600 hover:bg-blue-50 transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-gray-200 disabled:border-gray-200 disabled:text-gray-600"
                                onClick={() => handlePageClick(currentPage - 1)}
                                disabled={currentPage === 0}
                            >
                                이전
                            </button>
                            {renderPageNumbers()}
                            <button
                                className="px-4 py-2 mx-1 rounded-md border border-blue-600 bg-white text-blue-600 hover:bg-blue-50 transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-gray-200 disabled:border-gray-200 disabled:text-gray-600"
                                onClick={() => handlePageClick(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                            >
                                다음
                            </button>
                        </nav>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default NoticeListPage;
