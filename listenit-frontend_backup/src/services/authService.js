// src/services/authService.js
import api, { getAuthenticatedUser as apiGetAuthenticatedUser } from './api';

// JWT 토큰을 localStorage에서 가져오는 함수 (HttpOnly 쿠키는 JS에서 접근 불가)
export const getAuthToken = () => {
  return localStorage.getItem('jwt_token');
};

// 사용자 로그인 API 호출
export const login = async (username, password) => {
  try {
    const response = await api.post('/auth/authenticate', { username, password });
    const { accessToken, user } = response.data; // 백엔드 응답에서 accessToken과 user 객체 (UserDTO)를 받음

    localStorage.setItem('jwt_token', accessToken); // 받아온 Access Token을 localStorage에 저장
    localStorage.setItem('user_info', JSON.stringify(user)); // 사용자 정보 (UserDTO)도 localStorage에 저장

    return { success: true, user }; // 로그인 성공, 구성된 사용자 정보 반환
  } catch (error) {
    console.error('로그인 API 호출 오류:', error.response ? error.response.data : error.message);
    throw new Error(error.response?.data?.message || '로그인에 실패했습니다.');
  }
};

// 사용자 로그아웃 처리
export const logout = async () => {
  try {
    // 백엔드에 로그아웃 요청을 보냄 (서버 세션 무효화 및 HttpOnly 쿠키 제거 목적)
    await api.post('/logout'); // Spring Security 기본 로그아웃 URL
  } catch (error) {
    console.error('백엔드 로그아웃 실패:', error);
    // 오류가 발생하더라도 클라이언트 측에서는 사용자 정보를 제거하여 로그아웃 상태로 만듭니다.
  } finally {
    // 클라이언트 측 저장소에서 JWT 토큰과 사용자 정보 제거
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_info');
    // HttpOnly(false)로 설정된 쿠키를 수동으로 제거 (아니면 서버가 삭제하도록 함)
    document.cookie = "jwt_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    // refresh_token도 HttpOnly 쿠키라면 JS에서 직접 삭제는 어렵지만, 만약을 위해 시도
    document.cookie = "refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    console.log('클라이언트 측 로그아웃 완료');
  }
};

// 현재 로그인된 사용자 정보 가져오기
export const getCurrentUser = async () => {
  const token = getAuthToken(); // localStorage에서 JWT 토큰 가져오기

  if (!token) { // 토큰이 없으면 인증되지 않은 상태
    localStorage.removeItem('user_info'); // 혹시 모를 잔여 사용자 정보 제거
    return null;
  }

  // localStorage에서 먼저 사용자 정보 가져오기 (캐시)
  const userInfo = localStorage.getItem('user_info');
  if (userInfo) {
      try {
          // JSON 파싱 오류 방지를 위한 try-catch
          const parsedUserInfo = JSON.parse(userInfo);
          // 토큰이 존재하고 사용자 정보도 있다면, 유효성 검사를 위해 서버에 요청
          // 이 호출이 성공하면, 토큰은 유효하고 사용자도 인증된 것으로 간주
          // 이전에 불필요한 서버 호출을 막기 위해 여기에서 userInfo를 바로 리턴하는 로직이 있었지만,
          // 새로고침 시 401 문제가 발생하는 것을 고려하여, 토큰 유효성 검사를 위해 무조건 서버에 확인 요청
          await apiGetAuthenticatedUser(); // 서버에 토큰 유효성 검증 요청 (응답은 사용하지 않음)
          return parsedUserInfo; // 토큰 유효성 검증 성공 시 저장된 사용자 정보 반환
      } catch (e) {
          console.error("저장된 사용자 정보 파싱 오류 또는 서버 유효성 검증 실패:", e);
          // 파싱 오류나 서버 유효성 검증 실패 시 토큰 및 정보 삭제
          localStorage.removeItem('jwt_token');
          localStorage.removeItem('user_info');
          document.cookie = "jwt_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
          return null;
      }
  }

  // localStorage에 사용자 정보가 없으면 백엔드 API를 통해 가져옴 (토큰 유효성 검증 겸)
  try {
    const response = await apiGetAuthenticatedUser(); // /api/auth/me 호출
    const user = response.data; // UserDTO 형태
    if (user) {
      localStorage.setItem('user_info', JSON.stringify(user)); // 최신 사용자 정보로 localStorage 업데이트
      return user; // 사용자 정보 반환
    }
    return null;
  } catch (error) {
    // API 호출 실패 (예: 401 Unauthorized) 시 토큰이 유효하지 않다고 판단
    console.error("JWT 토큰 유효성 검사 실패 또는 사용자 정보 로드 오류 (백엔드 요청 실패):", error.response ? error.response.data : error.message);
    localStorage.removeItem('jwt_token'); // 토큰 및 정보 삭제
    localStorage.removeItem('user_info');
    document.cookie = "jwt_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    return null;
  }
};


// 사용자 회원가입 API 호출
export const register = async (userData) => {
  try {
    const response = await api.post('/signup', userData);
    return { success: true, message: '회원가입 성공', user: response.data };
  } catch (error) {
    console.error('회원가입 API 호출 오류:', error.response ? error.response.data : error.message);
    throw new Error(error.response?.data?.message || '회원가입에 실패했습니다.');
  }
};
