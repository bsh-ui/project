// src/App.js
import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useNavigate, useLocation } from 'react-router-dom'; // useNavigate, useLocation 추가
import { AuthProvider, useAuth } from './context/AuthContext'; // ⭐ 수정: './context/AuthContext' -> 'context/AuthContext'

import MainPage from './pages/MainPage'; // ⭐ 수정: './pages/MainPage' -> 'pages/MainPage'
import LoginPage from './pages/LoginPage'; // ⭐ 수정: './pages/LoginPage' -> 'pages/LoginPage'
import SignupPage from './pages/SignupPage'; // ⭐ 수정: './pages/SignupPage' -> 'pages/SignupPage'
import MyPage from './pages/MyPage'; // ⭐ 수정: './pages/MyPage' -> 'pages/MyPage'
import ForgotPassword from './pages/ForgotPassword'; // ⭐ 수정: './pages/ForgotPassword' -> 'pages/ForgotPassword'
import ProfilePage from './pages/ProfilePage'; // ⭐ 수정: './pages/ProfilePage' -> 'pages/ProfilePage'

// Music Pages
import MusicDetailPage from './pages/MusicDetailPage'; // ⭐ 수정: './pages/MusicDetailPage' -> 'pages/MusicDetailPage'
import MusicAdminPage from './pages/MusicAdminPage'; // ⭐ 수정: './pages/MusicAdminPage' -> 'pages/MusicAdminPage'
import MyPlaylists from './pages/MyPlaylists'; // ⭐ 수정: './pages/MyPlaylists' -> 'pages/MyPlaylists'

// Notice Pages
import NoticeListPage from './pages/NoticeListPage'; // ⭐ 수정: './pages/NoticeListPage' -> 'pages/NoticeListPage'
import NoticeDetail from './pages/NoticeDetail'; // ⭐ 수정: './pages/NoticeDetail' -> 'pages/NoticeDetail'
import NoticeAdminPage from './pages/NoticeAdminPage'; // ⭐ 수정: './pages/NoticeAdminPage' -> 'pages/NoticeAdminPage'

// Post Pages
import PostListPage from './pages/PostListPage'; // ⭐ 수정: './pages/PostListPage' -> 'pages/PostListPage'
import PostDetailPage from './pages/PostDetailPage'; // ⭐ 수정: './pages/PostDetailPage' -> 'pages/PostDetailPage'
import PostFormPage from './pages/PostFormPage'; // ⭐ 수정: './pages/PostFormPage' -> 'pages/PostFormPage'

// 404 Not Found Page
import NotFoundPage from './pages/NotFoundPage'; // ⭐ 수정: './pages/NotFoundPage' -> 'pages/NotFoundPage'

// AppContent 컴포넌트를 분리하여 useNavigate, useLocation, useAuth 훅을 사용할 수 있도록 합니다.
function AppContent() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, loading: authLoading } = useAuth(); // AuthContext에서 인증 상태 가져오기

  // 애플리케이션 초기 로드 시 로그인 상태를 확인하고 리다이렉트합니다.
  useEffect(() => {
    // 인증 로딩이 완료된 후, 로그인되지 않았다면 특정 페이지로의 접근을 제한하고 로그인 페이지로 리다이렉트합니다.
    // 로그인 없이 접근 가능한 경로 목록입니다.
    const publicPaths = [
      '/',
      '/main',
      '/login',
      '/signup',
      '/forgot-password',
      '/notice-list',
      '/posts' // 게시글 목록은 로그인 없이 접근 가능
    ];

    // 현재 경로가 공개 경로에 포함되지 않고, 동적 경로 (예: /music/:id, /notice-detail/:id, /posts/:id)도 아니면서
    // 인증 로딩이 완료되었는데 사용자가 인증되지 않았다면 로그인 페이지로 리다이렉트합니다.
    if (
      !authLoading &&
      !isAuthenticated &&
      !publicPaths.includes(location.pathname) &&
      !location.pathname.startsWith('/music/') && // music detail page
      !location.pathname.startsWith('/notice-detail/') && // notice detail page
      !location.pathname.startsWith('/posts/') // post detail page (read-only access for non-authenticated)
    ) {
      console.log("Not authenticated. Redirecting to login page.");
      navigate('/login'); // 로그인 페이지로 이동
    }
  }, [isAuthenticated, authLoading, navigate, location.pathname]); // 의존성 배열에 모든 관련 변수 포함

  return (
    <div className="App">
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/main" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/my-page" element={<MyPage />} /> {/* 마이페이지 */}
        <Route path="/forgot-password" element={<ForgotPassword />} /> {/* 비밀번호 찾기 */}
        <Route path="/profile" element={<ProfilePage />} /> {/* 프로필 사진 관리 */}

        {/* Music 관련 라우트 */}
        <Route path="/music/:id" element={<MusicDetailPage />} />
        <Route path="/admin/music" element={<MusicAdminPage />} />
        <Route path="/my-playlists" element={<MyPlaylists />} />

        {/* Notice 관련 라우트 */}
        <Route path="/notice-list" element={<NoticeListPage />} />
        <Route path="/notice-detail/:id" element={<NoticeDetail />} /> {/* 상세 페이지의 id는 파라미터로 */}
        <Route path="/admin/notice" element={<NoticeAdminPage />} />

        {/* Post 관련 라우트 */}
        <Route path="/posts" element={<PostListPage />} />
        <Route path="/posts/:id" element={<PostDetailPage />} />
        <Route path="/posts/new" element={<PostFormPage mode="create" />} />
        <Route path="/posts/edit/:id" element={<PostFormPage mode="edit" />} />

        {/* Fallback route for 404 Not Found */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </div>
  );
}

// App 컴포넌트: AuthProvider로 전체 앱을 감싸서 인증 컨텍스트를 제공합니다.
function App() {
  return (
    // <Router>
      <AuthProvider> {/* AuthProvider로 전체 앱을 감싸서 인증 컨텍스트 제공 */}
        <AppContent /> {/* AppContent 컴포넌트를 렌더링 */}
      </AuthProvider>
    // </Router>
  );
}

export default App;
