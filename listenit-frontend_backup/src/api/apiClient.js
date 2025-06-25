// src/api/apiClient.js
import axios from 'axios';

// API 기본 URL 설정
// 실제 백엔드 서버의 주소에 맞게 이 부분을 수정해야 합니다.
// 개발 환경에서는 'http://localhost:8485'를 사용하고, 배포 시에는 실제 도메인으로 변경하세요.
const API_BASE_URL = 'http://localhost:8485'; 

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000, // 요청 타임아웃 10초 (10000ms)
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // CORS 문제 해결 및 쿠키(세션) 전송을 위해 필요
});

// 요청 인터셉터: JWT 토큰을 요청 헤더에 자동으로 추가
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwtToken'); // localStorage에서 JWT 토큰 가져오기
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        // 요청 에러 처리
        return Promise.reject(error);
    }
);

// 응답 인터셉터: 공통 에러 처리 (예: 401 Unauthorized, 403 Forbidden)
apiClient.interceptors.response.use(
    (response) => {
        // 응답이 성공적일 경우 그대로 반환
        return response;
    },
    (error) => {
        if (error.response) {
            // 서버가 응답을 보냈지만 상태 코드가 2xx 범위를 벗어남
            console.error('API 응답 오류:', error.response.status, error.response.data);
            if (error.response.status === 401 || error.response.status === 403) {
                // 인증 만료 또는 권한 없음 처리
                console.warn('인증 또는 권한 오류 발생. 로그인 페이지로 리다이렉트합니다.');
                localStorage.removeItem('jwtToken'); // 유효하지 않은 토큰 제거
                // React Router의 navigate 훅을 사용해야 하므로, 여기서 직접 window.location.href를 사용하지 않습니다.
                // 대신, 이 에러를 호출한 컴포넌트에서 navigate를 통해 처리하도록 Promise.reject(error)를 반환합니다.
                // 또는 전역 Context 등에서 이 에러를 감지하여 리다이렉트할 수 있습니다.
            }
        } else if (error.request) {
            // 요청이 만들어졌지만 응답을 받지 못함 (예: 네트워크 오류)
            console.error('API 요청 오류: 응답 없음', error.request);
        } else {
            // 요청 설정 중 오류 발생
            console.error('API 요청 설정 오류:', error.message);
        }
        return Promise.reject(error); // 오류를 호출자에게 전파
    }
);

export default apiClient;
