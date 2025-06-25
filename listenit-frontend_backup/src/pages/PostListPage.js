// src/pages/PostListPage.js
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, PlusCircle, Eye, ThumbsUp, ThumbsDown } from 'lucide-react';
import { getPosts } from '../services/api'; // API 서비스 임포트
import './PostListPage.css'; // 스타일 시트 임포트

function PostListPage() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const navigate = useNavigate();

  // 게시글을 불러오는 비동기 함수
  const fetchPosts = async () => {
    setLoading(true);
    setError(null); // 에러 초기화
    try {
      // getPosts API 호출 시 검색어, 페이지, 사이즈 등을 전달
      const response = await getPosts(page, size, 'createdAt,desc', searchTerm);
      setPosts(response.data.content); // 백엔드의 Page 객체에서 content를 가져옴
      setTotalPages(response.data.totalPages); // 총 페이지 수 설정
    } catch (err) {
      console.error("게시글 로드 실패:", err);
      setError("게시글을 불러오는 데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 컴포넌트 마운트 시, 또는 page/size가 변경될 때마다 게시글을 다시 불러옴
  useEffect(() => {
    fetchPosts();
  }, [page, size]); // searchTerm이 변경될 때는 handleSearch에서 직접 fetchPosts 호출

  // 검색 버튼 클릭 또는 검색어 입력 후 Enter 시 실행
  const handleSearch = (e) => {
    e.preventDefault(); // 폼 기본 제출 동작 방지
    setPage(0); // 검색 시 첫 페이지부터 다시 시작
    fetchPosts(); // 새로운 검색어로 게시글 다시 불러오기
  };

  // 페이지 변경 핸들러
  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  // 로딩 중일 때 표시
  if (loading) {
    return <div className="loading-message">게시글을 불러오는 중...</div>;
  }

  // 에러 발생 시 표시
  if (error) {
    return <div className="error-message">{error}</div>;
  }

  return (
    <div className="post-list-page">
      <h2 className="page-title">자유 게시판</h2>
      <p className="page-description">음악에 대한 자유로운 생각과 정보를 공유해보세요!</p>

      {/* 검색 바 */}
      <form onSubmit={handleSearch} className="search-bar-container">
        <input
          type="text"
          placeholder="게시글 검색..."
          className="search-input"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <button type="submit" className="search-button">
          <Search size={20} /> 검색
        </button>
      </form>

      {/* 게시글 작성 버튼 */}
      <div className="action-buttons">
        <button className="create-post-button" onClick={() => navigate('/posts/new')}>
          <PlusCircle size={20} /> 게시글 작성
        </button>
      </div>

      {/* 게시글 목록 */}
      <div className="post-list-container">
        {posts.length > 0 ? (
          posts.map(post => (
            <div key={post.id} className="post-card">
              <h3 className="post-card-title" onClick={() => navigate(`/posts/${post.id}`)}>
                {post.title}
              </h3>
              <div className="post-card-meta">
                <span className="post-card-author">작성자: {post.authorNickname || post.authorUsername}</span>
                <span className="post-card-date">작성일: {new Date(post.createdAt).toLocaleDateString()}</span>
                <span className="post-card-views">
                  <Eye size={16} className="icon-eye" /> 조회수: {post.viewCount}
                </span>
                {/* 좋아요/싫어요 개수는 백엔드 PostResponseDto에 포함되어야 함 */}
                <span className="post-card-likes">
                  <ThumbsUp size={16} className="icon-like" /> {post.likesCount || 0}
                </span>
                <span className="post-card-dislikes">
                  <ThumbsDown size={16} className="icon-dislike" /> {post.dislikesCount || 0}
                </span>
              </div>
              {/* <p className="post-card-content">{post.content.substring(0, 100)}...</p> */}
            </div>
          ))
        ) : (
          <p className="no-posts-message">게시글이 없습니다. 첫 게시글을 작성해보세요!</p>
        )}
      </div>

      {/* 페이징네이션 */}
      <div className="pagination">
        <button className="pagination-button" onClick={() => handlePageChange(page - 1)} disabled={page === 0}>
          이전
        </button>
        <span className="pagination-info">{page + 1} / {totalPages}</span>
        <button className="pagination-button" onClick={() => handlePageChange(page + 1)} disabled={page >= totalPages - 1}>
          다음
        </button>
      </div>
    </div>
  );
}

export default PostListPage;
