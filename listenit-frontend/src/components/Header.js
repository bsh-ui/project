// src/components/Header.js
import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Home, List, PlusCircle, LogIn, LogOut, Music } from 'lucide-react';
import { useAuth } from '../context/AuthContext'; // 'context' 폴더 경로 수정
import '../styles/Header.css'; // CSS 파일 경로 수정

function Header() {
  const { isAuthenticated, logout, user, setError } = useAuth(); // setError 함수 추가
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      const result = await logout(); // AuthContext의 로그아웃 함수 호출
      if (result.success) {
        // 성공 메시지는 AuthContext에서 navigate 이후에 처리되므로 여기서는 특별히 할 일 없음
        console.log('로그아웃 되었습니다.');
      } else {
        // AuthContext에서 에러가 처리되므로 여기서는 콘솔에만 출력
        console.error('로그아웃 실패:', result.message);
        // 필요하다면 이곳에서 사용자에게 표시할 추가적인 메시지 설정
        setError(result.message); // AuthContext의 error 상태 업데이트
      }
    } catch (error) {
      console.error('로그아웃 중 오류:', error);
      setError(error.message || '로그아웃 중 알 수 없는 오류가 발생했습니다.');
    }
  };

  return (
    <header className="header-container">
      <div className="header-content">
        <Link to="/" className="logo">
          <h1>자유 게시판</h1>
          <p>"Rate Your Music" Community</p>
        </Link>

        <nav className="main-nav">
          <ul>
            <li>
                 <a href="/" className="main-music-site-link">
                <Music size={20} /> 음악 사이트 메인
              </a>
            </li>
            <li>
              <NavLink to="/" className={({ isActive }) => (isActive ? 'active-link' : '')}>
                <Home size={20} /> 홈
              </NavLink>
            </li>
            <li>
              <NavLink to="/posts" className={({ isActive }) => (isActive ? 'active-link' : '')}> {/* 게시글 목록 링크 수정 */}
                <List size={20} /> 게시글 목록
              </NavLink>
            </li>
            {isAuthenticated && (
              <li>
                <NavLink to="/posts/new" className={({ isActive }) => (isActive ? 'active-link' : '')}>
                  <PlusCircle size={20} /> 게시글 작성
                </NavLink>
              </li>
            )}
            {/* 마이페이지 및 공지사항 링크 추가 */}
            {isAuthenticated && (
              <li>
                <NavLink to="/my-page" className={({ isActive }) => (isActive ? 'active-link' : '')}>
                  마이페이지
                </NavLink>
              </li>
            )}
            <li>
              <NavLink to="/notice-list" className={({ isActive }) => (isActive ? 'active-link' : '')}>
                공지사항
              </NavLink>
            </li>
          </ul>
        </nav>

        <div className="auth-actions">
          {isAuthenticated ? (
            <>
              <span className="user-info">{user?.nickname || user?.username}님</span>
              <button className="auth-button" onClick={handleLogout}>
                <LogOut size={20} /> 로그아웃
              </button>
            </>
          ) : (
            <Link to="/login" className="auth-button">
              <LogIn size={20} /> 로그인
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
