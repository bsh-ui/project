// src/context/AuthContext.js
import React, { createContext, useState, useEffect, useContext } from 'react';
import { login as authServiceLogin, logout as authServiceLogout, getCurrentUser } from '../services/authService';
import { useNavigate } from 'react-router-dom';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // 앱 시작 시 또는 새로고침 시 인증 상태를 확인하는 useEffect
  useEffect(() => {
    const checkAuth = async () => {
      setLoading(true);
      try {
        const currentUser = await getCurrentUser(); // authService에서 사용자 정보 가져오기 시도
        if (currentUser) {
          setIsAuthenticated(true);
          setUser(currentUser);
        } else {
          setIsAuthenticated(false);
          setUser(null);
        }
      } catch (err) {
        console.error('인증 상태 확인 실패:', err);
        setIsAuthenticated(false);
        setUser(null);
        // 에러를 사용자에게 직접 보여주기보다, 로그인 페이지로 리다이렉트되도록 하는 것이 일반적
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []); // 빈 의존성 배열: 컴포넌트 마운트 시 한 번만 실행

  const login = async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      const result = await authServiceLogin(username, password);
      if (result.success) {
        setIsAuthenticated(true);
        setUser(result.user);
        navigate('/'); // 로그인 성공 시 게시글 목록(홈)으로 이동
      } else {
        setError(result.error || '로그인 실패');
      }
    } catch (err) {
      console.error('로그인 중 오류:', err);
      setError(err.message || '로그인 중 오류가 발생했습니다.');
      throw err; // 에러를 호출자에게 다시 던짐
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setLoading(true);
    try {
      await authServiceLogout();
      setIsAuthenticated(false);
      setUser(null);
      navigate('/login');
    } catch (err) {
      console.error('로그아웃 실패:', err);
      alert('로그아웃 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const authContextValue = {
    isAuthenticated,
    user,
    loading,
    error,
    login,
    logout,
  };

  if (loading) {
    // 앱이 처음 로딩될 때 인증 상태를 확인하는 동안 로딩 스피너 등을 보여줄 수 있습니다.
    return <div className="flex justify-center items-center h-screen text-lg font-semibold">인증 정보 로딩 중...</div>;
  }

  return (
    <AuthContext.Provider value={authContextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth는 AuthProvider 내에서 사용되어야 합니다.');
  }
  return context;
};
