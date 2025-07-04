    // src/App.js
    import React from 'react';
    import { Routes, Route } from 'react-router-dom';
    import Header from './components/Header';
    import PostListPage from './pages/PostListPage';
    import PostDetailPage from './pages/PostDetailPage';
    import PostFormPage from './pages/PostFormPage';
    import LoginPage from './pages/LoginPage';
    import NotFoundPage from './pages/NotFoundPage'; // 404 페이지
    import { AuthProvider } from './context/AuthContext'; // 인증 컨텍스트

    function App() {
      return (
        <AuthProvider> {/* 앱 전체에 인증 컨텍스트 제공 */}
          <div className="App">
            <Header /> {/* 모든 페이지에 공통 헤더 */}

            <main className="container mx-auto p-4"> {/* 메인 콘텐츠 영역 */}
              {/* Routes 컴포넌트는 애플리케이션의 모든 라우트를 정의합니다. */}
              <Routes>
                {/* 게시글 목록 페이지: 기본 경로로 설정 */}
                <Route path="/" element={<PostListPage />} />
                {/* 게시글 상세 페이지: ID를 파라미터로 받음 */}
                <Route path="/posts/:id" element={<PostDetailPage />} />
                {/* 새 게시글 작성 페이지 */}
                <Route path="/posts/new" element={<PostFormPage mode="create" />} />
                {/* 게시글 수정 페이지: ID를 파라미터로 받음 */}
                <Route path="/posts/:id/edit" element={<PostFormPage mode="edit" />} />
                {/* 로그인 페이지 */}
                <Route path="/login" element={<LoginPage />} />
                {/* 정의되지 않은 모든 경로를 위한 404 페이지 */}
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </main>
          </div>
        </AuthProvider>
      );
    }

    export default App;
    