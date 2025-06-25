// src/components/Header.js
import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Home, List, PlusCircle, LogIn, LogOut,Music } from 'lucide-react';
import { useAuth } from '../context/AuthContext'; // '../context' 폴더가 곧 생성될 예정
import './Header.css'; // './Header.css' 파일도 곧 생성될 예정

function Header() {
  const { isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout(); // AuthContext의 로그아웃 함수 호출
      alert('로그아웃 되었습니다.');
      navigate('/login'); // 로그아웃 후 로그인 페이지로 이동
    } catch (error) {
      console.error('로그아웃 실패:', error);
      alert('로그아웃 중 오류가 발생했습니다.');
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
              <NavLink to="/" className={({ isActive }) => (isActive ? 'active-link' : '')}>
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
