// src/services/api.js
import axios from 'axios';
import { getAuthToken } from './authService'; // JWT 토큰 가져오는 함수

// ✅ 1. Axios 인스턴스 생성 및 인터셉터 설정
const apiClient = axios.create({
  baseURL: 'http://localhost:8485/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// JWT 토큰 자동 추가
apiClient.interceptors.request.use((config) => {
  const token = getAuthToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

// 401 응답 처리
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      // TODO: refresh token 로직 추가하거나 로그아웃 처리
      return Promise.reject(error);
    }
    return Promise.reject(error);
  }
);

// ✅ 2. API 함수들 정의 (도메인별)

// 게시글 API
export const getPosts = (page = 0, size = 10, sort = 'createdAt,desc', search = '') =>
  apiClient.get('/posts', { params: { page, size, sort, search } });

export const getPostById = (id) => apiClient.get(`/posts/${id}`);
export const createPost = (postData) => apiClient.post('/posts', postData);
export const updatePost = (id, postData) => apiClient.put(`/posts/${id}`, postData);
export const deletePost = (id) => apiClient.delete(`/posts/${id}`);

// 댓글 API
export const createComment = (postId, commentData) =>
  apiClient.post(`/posts/${postId}/comments`, commentData);

export const deleteComment = (postId, commentId) =>
  apiClient.delete(`/posts/${postId}/comments/${commentId}`);

// 좋아요/싫어요
export const createLike = (postId) => apiClient.post(`/posts/${postId}/likes`);
export const createDislike = (postId) => apiClient.post(`/posts/${postId}/dislikes`);

// 사용자/인증
export const loginUser = (credentials) => apiClient.post('/auth/login', credentials);
export const signupUser = (userData) => apiClient.post('/auth/signup', userData);
export const resetPasswordRequest = (email) => apiClient.post('/auth/reset-password-request', { email });

export const checkUsernameDuplicate = (username) => apiClient.get('/check-username', { params: { username } });
export const checkEmailDuplicate = (email) => apiClient.get('/check-email', { params: { email } });
export const checkNicknameDuplicate = (nickname) => apiClient.get('/check-nickname', { params: { nickname } });

export const getAuthenticatedUser = () => apiClient.get('/auth/me');
export const updateMyProfile = (data) => apiClient.put('/user/profile', data);
export const changeMyPassword = (data) => apiClient.post('/auth/change-password', data);
export const withdrawAccount = () => apiClient.delete('/auth/withdraw');

// 파일/이미지
export const uploadProfileImage = (formData) =>
  apiClient.post('/files/profile-image/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const fetchProfileImagePath = () => apiClient.get('/files/profile-image');
export const deleteProfileImage = () => apiClient.delete('/files/profile-image');

// 음악
export const getMusicList = (page = 0, size = 12, sort = 'createdAt,desc', keyword = '') =>
  apiClient.get('/music', { params: { page, size, sort, keyword } });

export const getMusicById = (id) => apiClient.get(`/music/${id}`);

export const uploadMusic = (formData) =>
  apiClient.post('/music/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const updateMusic = (id, formData) =>
  apiClient.put(`/music/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const deleteMusic = (id) => apiClient.delete(`/music/${id}`);

export const streamMusic = (id) =>
  apiClient.get(`/music/stream/${id}`, { responseType: 'blob' });

export const getMusicCoverImage = (fileName) =>
  `${apiClient.defaults.baseURL}/files/cover-image/${fileName}`;

// 플레이리스트
export const getMyPlaylists = () => apiClient.get('/playlists/my');
export const createPlaylist = (data) => apiClient.post('/playlists', data);
export const updatePlaylist = (id, data) => apiClient.put(`/playlists/${id}`, data);
export const deletePlaylist = (id) => apiClient.delete(`/playlists/${id}`);
export const getPlaylistDetail = (id) => apiClient.get(`/playlists/${id}`);
export const addMusicToPlaylist = (playlistId, musicId) =>
  apiClient.post(`/playlists/${playlistId}/music/${musicId}`);
export const removeMusicFromPlaylist = (playlistId, musicId) =>
  apiClient.delete(`/playlists/${playlistId}/music/${musicId}`);

// 공지사항
export const getNotices = (page = 0, size = 10, sort = 'createdAt,desc', type = '') =>
  apiClient.get('/notices', { params: { page, size, sort, type } });

export const getNoticeById = (id) => apiClient.get(`/notices/${id}`);
export const createNotice = (data) => apiClient.post('/notices', data);
export const updateNotice = (id, data) => apiClient.put(`/notices/${id}`, data);
export const deleteNotice = (id) => apiClient.delete(`/notices/${id}`);

// ✅ 기본 axios 인스턴스도 export (필요 시 커스텀 호출용)
export default apiClient;
