// src/pages/PostFormPage.js
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Send, Save, XCircle } from 'lucide-react';
import { getPostById, createPost, updatePost } from '../services/api'; // API 서비스 임포트
import { useAuth } from '../context/AuthContext'; // AuthContext에서 인증 상태 가져오기
import './PostFormPage.css'; // 게시글 폼 스타일 시트 임포트

// mode: 'create' 또는 'edit'
function PostFormPage({ mode }) {
  const { id } = useParams(); // URL 파라미터에서 게시글 ID (수정 모드일 때 사용)
  const navigate = useNavigate();
  const { isAuthenticated, loading: authLoading, user } = useAuth(); // 인증 상태 및 사용자 정보 가져오기

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false); // API 호출 로딩 상태
  const [error, setError] = useState(null); // 에러 메시지

  // 컴포넌트 마운트 시, 또는 mode/id/user/authLoading이 변경될 때 데이터 불러오기
  useEffect(() => {
    // 인증 로딩이 완료되고 로그인되지 않았다면 로그인 페이지로 리다이렉트
    if (!authLoading && !isAuthenticated) {
      alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
      navigate('/login');
      return; // 더 이상 진행하지 않음
    }

    if (mode === 'edit' && id && isAuthenticated && user) {
      const fetchPostData = async () => {
        setLoading(true);
        setError(null);
        try {
          const response = await getPostById(id);
          const postData = response.data;

          // 게시글 작성자와 현재 로그인된 사용자가 일치하는지 확인
          if (user.id === postData.authorId) {
            setTitle(postData.title);
            setContent(postData.content);
          } else {
            // 작성자가 아니면 수정 페이지 접근 거부
            setError('게시글을 수정할 권한이 없습니다.');
            alert('게시글은 작성자만 수정할 수 있습니다.');
            navigate(`/posts/${id}`); // 게시글 상세 페이지로 리다이렉션
          }
        } catch (err) {
          console.error("게시글 불러오기 실패:", err);
          setError("게시글 정보를 불러오는 데 실패했습니다.");
        } finally {
          setLoading(false);
        }
      };
      fetchPostData();
    } else if (mode === 'create') {
      // 생성 모드일 경우 필드를 비웁니다.
      setTitle('');
      setContent('');
    }
  }, [mode, id, navigate, isAuthenticated, user, authLoading]); // 의존성 배열 업데이트

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // 다시 한번 인증 확인 (클라이언트 측 유효성)
    if (!isAuthenticated) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (!title.trim() || !content.trim()) {
      setError('제목과 내용을 모두 입력해주세요.');
      return;
    }

    setLoading(true);
    try {
      const postData = { title, content }; // Spring Boot 백엔드는 authorId를 @AuthenticationPrincipal로 추출

      let response;
      if (mode === 'create') {
        response = await createPost(postData);
        alert('게시글이 성공적으로 작성되었습니다!');
      } else { // 'edit' mode
        response = await updatePost(id, postData);
        alert('게시글이 성공적으로 수정되었습니다!');
      }
      navigate(`/posts/${response.data.id}`); // 새로 생성/수정된 게시글 상세 페이지로 이동
    } catch (err) {
      console.error("게시글 저장 실패:", err.response ? err.response.data : err.message);
      const errorMessage = err.response?.data?.message || err.response?.data || err.message || "게시글 저장에 실패했습니다.";
      setError(errorMessage);
      alert('게시글 저장에 실패했습니다. ' + errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 인증 정보 로딩 중이거나, 수정 모드에서 게시글 정보 로딩 중일 때 로딩 메시지 표시
  if (authLoading || (loading && mode === 'edit' && !title)) {
    return <div className="loading-message">정보를 불러오는 중...</div>;
  }

  // 로그인되지 않았다면 로그인 페이지로 리다이렉트되므로 이 부분은 거의 표시되지 않습니다.
  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="post-form-page">
      <h2 className="form-title">{mode === 'create' ? '새 게시글 작성' : '게시글 수정'}</h2>
      {error && <p className="error-message">{error}</p>}
      <form onSubmit={handleSubmit} className="post-form">
        <div className="form-group">
          <label htmlFor="title" className="form-label">제목</label>
          <input
            type="text"
            id="title"
            className="form-input"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="게시글 제목을 입력하세요"
            required
            disabled={loading} // API 호출 중에는 입력 비활성화
          />
        </div>
        <div className="form-group">
          <label htmlFor="content" className="form-label">내용</label>
          <textarea
            id="content"
            className="form-textarea"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="게시글 내용을 입력하세요"
            rows="10"
            required
            disabled={loading} // API 호출 중에는 입력 비활성화
          ></textarea>
        </div>
        <div className="form-actions">
          <button type="submit" className="submit-button" disabled={loading}>
            {loading ? '저장 중...' : (mode === 'create' ? <><Send size={20} /> 작성 완료</> : <><Save size={20} /> 수정 완료</>)}
          </button>
          <button type="button" className="cancel-button" onClick={() => navigate(-1)} disabled={loading}>
            <XCircle size={20} /> 취소
          </button>
        </div>
      </form>
    </div>
  );
}

export default PostFormPage;
