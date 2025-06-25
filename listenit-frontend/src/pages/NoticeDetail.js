import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom'; // React Router 사용 가정
import 'bootstrap/dist/css/bootstrap.min.css'; // Bootstrap CSS 임포트
import '../styles/NoticeDetail.css'; // 별도의 CSS 파일 (아래에 제공)

function NoticeDetail() {
    const { id } = useParams(); // URL 파라미터에서 ID 가져오기 (React Router 필요)
    const navigate = useNavigate(); // 페이지 이동을 위한 useNavigate 훅 (React Router 필요)
    const [notice, setNotice] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchNoticeDetail = async () => {
            if (!id) {
                setError('오류: 공지사항 ID가 없습니다.');
                setLoading(false);
                return;
            }

            try {
                const response = await fetch(`/api/notices/${id}`);
                if (response.status === 404) {
                    setError('공지사항을 찾을 수 없습니다.');
                    setLoading(false);
                    return;
                }
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data = await response.json();
                setNotice(data);
            } catch (err) {
                console.error('Error fetching notice detail:', err);
                setError('오류: 상세 내용을 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.');
            } finally {
                setLoading(false);
            }
        };

        fetchNoticeDetail();
    }, [id]); // id가 변경될 때마다 effect 재실행

    if (loading) {
        return <div className="container mt-5 text-center">로딩 중...</div>;
    }

    if (error) {
        return (
            <div className="container mt-5">
                <div className="card">
                    <div className="card-body">
                        <h1 className="notice-title text-danger">{error}</h1>
                        <p>목록에서 올바른 공지사항을 선택해주세요.</p>
                        <div className="back-button">
                            <button className="btn btn-secondary" onClick={() => navigate('/notice_list')}>
                                목록으로 돌아가기
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (!notice) {
        return null; // 데이터가 없으면 아무것도 렌더링하지 않음
    }

    return (
        <div className="container">
            <div className="card">
                <div className="card-header" id="notice-type-header">
                    {notice.type === 'NOTICE' ? '공지사항' : '이벤트'} 상세
                </div>
                <div className="card-body">
                    <h1 className="notice-title" id="notice-title">{notice.title}</h1>
                    <div className="notice-meta">
                        <span id="notice-author">작성자: {notice.authorUsername}</span> |
                        <span id="notice-view-count"> 조회수: {notice.viewCount}</span> |
                        <span id="notice-created-at"> 작성일: {new Date(notice.createdAt).toLocaleDateString()}</span>
                    </div>
                    <div
                        className="notice-content"
                        id="notice-content"
                        dangerouslySetInnerHTML={{ __html: notice.content.replace(/\n/g, '<br>') }}
                    ></div>
                </div>
                <div className="card-footer bg-transparent border-0 back-button">
                    <button className="btn btn-secondary" onClick={() => navigate('/notice_list')}>
                        목록으로 돌아가기
                    </button>
                </div>
            </div>
        </div>
    );
}

export default NoticeDetail;