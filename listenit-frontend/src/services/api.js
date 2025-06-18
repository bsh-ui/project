// src/services/api.js
import axios from 'axios';
import { getAuthToken } from './authService'; // authService.js에서 JWT 토큰 가져오는 함수 임포트

// Axios 인스턴스 생성
const api = axios.create({
  baseURL: 'http://localhost:8485/api', // Spring Boot 백엔드 API 기본 URL
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // CORS 문제 해결 및 쿠키 전송을 위함
});

// 요청 인터셉터: 모든 요청에 JWT 토큰을 추가 (로그인된 경우)
api.interceptors.request.use(
  (config) => {
    const token = getAuthToken(); // authService에서 토큰 가져오기
    if (token) {
      config.headers.Authorization = `Bearer ${token}`; // Authorization 헤더에 Bearer 토큰 추가
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터: 401 Unauthorized 응답 처리 (토큰 만료 등)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    // 401 Unauthorized 에러이고, 재시도되지 않은 요청인 경우 (무한 루프 방지)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true; // 재시도 플래그 설정

      // ⭐ 여기서는 Refresh Token 로직을 구현하지 않고,
      // 단순히 로그아웃 처리 후 로그인 페이지로 리디렉션합니다.
      // 실제 서비스에서는 Refresh Token을 사용하여 Access Token을 재발급받는 로직이 필요합니다.
      // console.warn('401 Unauthorized 응답 수신. 토큰 만료 또는 유효하지 않음.');
      // alert('세션이 만료되었거나 인증되지 않았습니다. 다시 로그인해주세요.');

      // AuthContext의 logout 함수를 직접 import해서 사용하기 어렵기 때문에,
      // localStorage를 직접 지우고 페이지 새로고침 (또는 로그인 페이지로 리디렉션)
      // localStorage.removeItem('jwt_token');
      // localStorage.removeItem('user_info');
      // document.cookie = "jwt_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"; // 쿠키 삭제 시도
      // window.location.href = '/login'; // 로그인 페이지로 강제 이동

      return Promise.reject(error); // 원래 에러를 다시 던짐
    }
    return Promise.reject(error);
  }
);


// 게시글 관련 API 함수들
export const getPosts = (page = 0, size = 10, sort = 'createdAt,desc', search = '') => {
  return api.get(`/posts`, {
    params: { page, size, sort, search }
  });
};

export const getPostById = (id) => {
  return api.get(`/posts/${id}`);
};

export const createPost = (postData) => {
  return api.post('/posts', postData);
};

export const updatePost = (id, postData) => {
  return api.put(`/posts/${id}`, postData);
};

export const deletePost = (id) => {
  return api.delete(`/posts/${id}`);
};

// 댓글 관련 API 함수들
export const createComment = (postId, commentData) => {
  return api.post(`/posts/${postId}/comments`, commentData);
};

export const deleteComment = (postId, commentId) => {
  return api.delete(`/posts/${postId}/comments/${commentId}`);
};

// 좋아요/싫어요 관련 API 함수들
export const createLike = (postId) => {
  return api.post(`/posts/${postId}/likes`);
};

export const createDislike = (postId) => {
  return api.post(`/posts/${postId}/dislikes`);
};

// 사용자 정보 관련 (현재 로그인된 사용자 정보 가져오기)
// AuthContext와 authService.js에서 사용될 예정
export const getAuthenticatedUser = () => {
  return api.get('/auth/me'); // ⭐ 변경: 백엔드와 일치하는 엔드포인트: /api/auth/me로 수정 ⭐
};

export default api; // 기본 Axios 인스턴스 내보내기
