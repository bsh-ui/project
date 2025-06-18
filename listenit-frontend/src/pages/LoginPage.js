// src/pages/LoginPage.js
import React, { useState } from 'react';
import { LogIn as LogInIcon, User, Lock } from 'lucide-react';
import { useAuth } from '../context/AuthContext'; // AuthContext 임포트
import './LoginPage.css'; // 로그인 페이지 스타일 시트 임포트

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { login, error: authError, loading: authLoading } = useAuth(); // AuthContext에서 login 함수와 에러/로딩 상태 가져오기

  // 자체 에러 메시지 (클라이언트 측 유효성 검사 등)
  const [localError, setLocalError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault(); // 기본 폼 제출 방지
    setLocalError(null); // 이전 로컬 에러 초기화

    if (!username.trim() || !password.trim()) {
      setLocalError('아이디와 비밀번호를 모두 입력해주세요.');
      return;
    }

    try {
      // AuthContext의 login 함수 호출
      // 이 함수는 authService.login을 통해 백엔드 JSON 로그인 API를 호출합니다.
      await login(username, password);
      // 로그인 성공 시 AuthContext 내부에서 /board 페이지로 리디렉션 처리됩니다.
    } catch (err) {
      // AuthContext의 login 함수에서 이미 에러를 설정하고 throw하므로 여기서는 추가 처리 불필요
      // (AuthContext의 error 상태를 사용합니다)
      console.error('로그인 제출 중 오류:', err);
    }
  };

  return (
    <div className="login-page">
      <h2 className="login-title">로그인</h2>
      {/* 로컬 에러 또는 AuthContext의 에러 메시지를 표시 */}
      {(localError || authError) && (
        <p className="error-message">{localError || authError}</p>
      )}

      <form onSubmit={handleSubmit} className="login-form">
        <div className="form-group">
          <label htmlFor="username" className="form-label">
            <User size={18} /> 아이디 (이메일)
          </label>
          <input
            type="text"
            id="username"
            name="username" // Spring Security에서 읽는 필드명 (기본)
            className="form-input"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            disabled={authLoading} // 로그인 처리 중에는 입력 비활성화
            placeholder="이메일 주소"
          />
        </div>
        <div className="form-group">
          <label htmlFor="password" className="form-label">
            <Lock size={18} /> 비밀번호
          </label>
          <input
            type="password"
            id="password"
            name="password" // Spring Security에서 읽는 필드명 (기본)
            className="form-input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={authLoading} // 로그인 처리 중에는 입력 비활성화
            placeholder="비밀번호"
          />
        </div>
        <button type="submit" className="login-button" disabled={authLoading}>
          {authLoading ? '로그인 중...' : <><LogInIcon size={20} /> 로그인</>}
        </button>
      </form>

      <div className="signup-link">
        <p>계정이 없으신가요? <span onClick={() => alert('회원가입 페이지로 이동 (나중에 구현)')} className="link-text">회원가입</span></p>
        <p>비밀번호를 잊으셨나요? <span onClick={() => alert('비밀번호 찾기 페이지로 이동 (나중에 구현)')} className="link-text">비밀번호 찾기</span></p>
      </div>
    </div>
  );
}

export default LoginPage;
