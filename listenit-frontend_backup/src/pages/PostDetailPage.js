// src/pages/PostDetailPage.js
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Edit, Trash2, ArrowLeft, MessageSquare, ThumbsUp, ThumbsDown } from 'lucide-react';
import { getPostById, deletePost, createComment, deleteComment, createLike, createDislike } from '../services/api';
import { useAuth } from '../context/AuthContext';
import './PostDetailPage.css'; // PostDetailPage 스타일 시트

// CommentItem 컴포넌트: 재귀적으로 대댓글을 렌더링합니다.
const CommentItem = ({ comment, postId, onDeleteComment, onReplySubmit, currentUser }) => {
  const [replyContent, setReplyContent] = useState('');
  const [showReplyForm, setShowReplyForm] = useState(false);
  const isCommentAuthor = currentUser && currentUser.id === comment.authorId; // 댓글 작성자 확인

  const handleReplySubmit = async (e) => {
    e.preventDefault();
    if (!replyContent.trim()) {
      alert('답글 내용을 입력해주세요.');
      return;
    }
    // 부모 댓글의 ID를 parentId로 전달하여 대댓글임을 명시
    await onReplySubmit(postId, replyContent, comment.id);
    setReplyContent('');
    setShowReplyForm(false); // 답글 작성 후 폼 숨기기
  };

  return (
    <div className={`comment-item ${comment.parentId ? 'reply-item' : ''}`}>
      <p className="comment-author">{comment.authorNickname || comment.authorUsername}</p>
      <p className="comment-content">{comment.content}</p>
      <div className="comment-actions-meta">
        <span className="comment-date">{new Date(comment.createdAt).toLocaleString()}</span>
        {currentUser && (
          <>
            <button className="comment-action-button" onClick={() => setShowReplyForm(!showReplyForm)}>
              답글
            </button>
            {isCommentAuthor && (
              <button className="comment-action-button delete-comment-button" onClick={() => onDeleteComment(postId, comment.id)}>
                삭제
              </button>
            )}
          </>
        )}
      </div>

      {showReplyForm && (
        <form onSubmit={handleReplySubmit} className="reply-form">
          <textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="답글을 작성하세요..."
            rows="2"
          ></textarea>
          <button type="submit" className="submit-reply-button">
            답글 달기
          </button>
        </form>
      )}

      {/* 대댓글이 있다면 재귀적으로 CommentItem 렌더링 */}
      {comment.children && comment.children.length > 0 && (
        <div className="nested-comments">
          {comment.children.map(reply => (
            <CommentItem
              key={reply.id}
              comment={reply}
              postId={postId}
              onDeleteComment={onDeleteComment}
              onReplySubmit={handleReplySubmit}
              currentUser={currentUser}
            />
          ))}
        </div>
      )}
    </div>
  );
};

function PostDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth(); // user 객체는 { id, username, nickname, email, roles } 구조
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [commentContent, setCommentContent] = useState('');

  // 게시글을 불러오는 함수 (좋아요/싫어요/댓글 후 재호출용)
  const fetchPost = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getPostById(id);
      setPost(response.data);
    } catch (err) {
      console.error("게시글 상세 로드 실패:", err);
      setError("게시글을 불러오는 데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 컴포넌트 마운트 시 또는 ID 변경 시 게시글 불러오기
  useEffect(() => {
    fetchPost();
  }, [id]); // id가 변경될 때마다 게시글을 다시 불러옴. fetchPost 함수는 의존성 배열에 포함되어야 함 (eslint 경고 해결)

  const handleDeletePost = async () => {
    if (window.confirm('정말 이 게시글을 삭제하시겠습니까?')) {
      try {
        await deletePost(id);
        alert('게시글이 삭제되었습니다.');
        navigate('/'); // 삭제 후 목록 페이지로 이동
      } catch (err) {
        console.error("게시글 삭제 실패:", err);
        alert('게시글 삭제에 실패했습니다.');
      }
    }
  };

  const handleCommentSubmit = async (targetPostId, content, parentId = null) => {
    if (!content.trim()) {
      alert('댓글 내용을 입력해주세요.');
      return;
    }
    try {
      await createComment(targetPostId, { content, parentId });
      alert('댓글이 작성되었습니다.');
      setCommentContent(''); // 댓글 입력 필드 초기화
      fetchPost(); // ⭐ 댓글 작성 후 게시글 데이터 새로고침 ⭐
    } catch (err) {
      console.error("댓글 작성 실패:", err);
      alert('댓글 작성에 실패했습니다.');
    }
  };

  const handleDeleteComment = async (targetPostId, commentIdToDelete) => {
    if (window.confirm('정말 이 댓글을 삭제하시겠습니까?')) {
      try {
        await deleteComment(targetPostId, commentIdToDelete);
        alert('댓글이 삭제되었습니다.');
        fetchPost(); // ⭐ 댓글 삭제 후 게시글 데이터 새로고침 ⭐
      } catch (err) {
        console.error("댓글 삭제 실패:", err);
        alert('댓글 삭제에 실패했습니다.');
      }
    }
  };

  const handleLike = async (isLike) => {
    if (!isAuthenticated) { // 로그인 여부 확인
      alert('좋아요/싫어요는 로그인 후 이용할 수 있습니다.');
      navigate('/login');
      return;
    }
    try {
      if (isLike) {
        await createLike(id);
        alert('좋아요를 눌렀습니다!');
      } else {
        await createDislike(id);
        alert('싫어요를 눌렀습니다!');
      }
      fetchPost(); // ⭐ 좋아요/싫어요 후 게시글 데이터 새로고침 ⭐
    } catch (err) {
      console.error("좋아요/싫어요 실패:", err);
      // 서버에서 이미 좋아요/싫어요를 눌렀을 때의 에러 처리가 있다면 추가
      alert('오류가 발생했습니다. 이미 좋아요/싫어요를 눌렀거나, 문제가 발생했습니다.');
    }
  };

  if (loading) {
    return <div className="loading-message">게시글 상세 정보를 불러오는 중...</div>;
  }

  if (error) {
    return <div className="error-message">{error}</div>;
  }

  if (!post) {
    return (
      <div className="post-detail-page">
        <p className="not-found-message">게시글을 찾을 수 없습니다.</p>
        <button className="back-button" onClick={() => navigate('/')}>
          <ArrowLeft size={20} /> 목록으로
        </button>
      </div>
    );
  }

  // user.id는 AuthContext에서 제공되는 사용자 정보의 id 필드입니다.
  // post.authorId는 PostResponseDto에서 받아오는 작성자의 id 필드입니다.
  const isAuthor = isAuthenticated && user && user.id === post.authorId;

  return (
    <div className="post-detail-page">
      <div className="detail-header">
        <h2 className="detail-title">{post.title}</h2>
        <div className="detail-meta">
          <span className="author-info">작성자: {post.authorNickname || post.authorUsername}</span>
          <span className="date-info">작성일: {new Date(post.createdAt).toLocaleString()}</span>
          <span className="views-info">조회수: {post.viewCount}</span>
        </div>
      </div>

      <div className="detail-content-section">
        <p className="detail-content">{post.content}</p>
      </div>

      <div className="detail-actions">
        {/* 좋아요/싫어요 버튼은 로그인 상태에 따라 항상 표시 */}
        {/* 단, 본인 게시글에는 좋아요/싫어요를 못 누르게 하려면 isAuthor 조건 추가 */}
        {isAuthenticated && (
          <>
            <button className="action-button like-button" onClick={() => handleLike(true)}>
              <ThumbsUp size={20} /> 좋아요 ({post.likesCount || 0})
            </button>
            <button className="action-button dislike-button" onClick={() => handleLike(false)}>
              <ThumbsDown size={20} /> 싫어요 ({post.dislikesCount || 0})
            </button>
          </>
        )}
        {isAuthor && ( // 게시글 작성자에게만 수정/삭제 버튼 표시
          <>
            <button className="action-button edit-button" onClick={() => navigate(`/posts/${post.id}/edit`)}>
              <Edit size={20} /> 수정
            </button>
            <button className="action-button delete-button" onClick={handleDeletePost}>
              <Trash2 size={20} /> 삭제
            </button>
          </>
        )}
        <button className="action-button back-button" onClick={() => navigate('/')}>
          <ArrowLeft size={20} /> 목록으로
        </button>
      </div>

      <div className="comments-section">
        <h3>댓글 ({post.comments?.length || 0})</h3>
        {isAuthenticated && ( // 로그인된 사용자만 댓글 폼 표시
          <form onSubmit={(e) => { e.preventDefault(); handleCommentSubmit(post.id, commentContent); }} className="comment-form">
            <textarea
              value={commentContent}
              onChange={(e) => setCommentContent(e.target.value)}
              placeholder="댓글을 작성하세요..."
              rows="3"
            ></textarea>
            <button type="submit" className="submit-comment-button">
              <MessageSquare size={18} /> 댓글 달기
            </button>
          </form>
        )}
        <div className="comment-list">
          {post.comments && post.comments.length > 0 ? (
            // 최상위 댓글만 먼저 렌더링하고, CommentItem 내부에서 재귀적으로 대댓글 렌더링
            // 백엔드에서 부모-자식 관계가 명확히 처리되어 반환된다고 가정
            post.comments.filter(comment => !comment.parentId).map(comment => (
              <CommentItem
                key={comment.id}
                comment={comment}
                postId={post.id}
                onDeleteComment={handleDeleteComment}
                onReplySubmit={handleCommentSubmit}
                currentUser={user} // 현재 로그인된 사용자 정보 전달
              />
            ))
          ) : (
            <p className="no-comments-message">아직 댓글이 없습니다. 첫 댓글을 달아보세요!</p>
          )}
        </div>
      </div>
    </div>
  );
}

export default PostDetailPage;
