// src/context/AuthContext.js
import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient'; // Axios 인스턴스 임포트

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    // user는 { username, nickname, roles, profilePictureUrl } 형태의 객체
    const [user, setUser] = useState(null);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true); // 초기 인증 상태 로딩 여부
    const [error, setError] = useState(null); // 인증 관련 에러 메시지
    const navigate = useNavigate();

    // JWT 토큰을 쿠키에서 파싱하는 함수
    // HttpOnly 쿠키는 JS에서 직접 값을 읽을 수 없으므로, 이 함수는 단순 존재 여부 확인용
    const getCookie = (name) => {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for(let i=0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return c.indexOf(nameEQ) === 0; // 값 대신 존재 여부만 반환
        }
        return false; // 쿠키 없음
    };

    // JWT 토큰 유효성 검사 및 사용자 정보 가져오는 함수
    const fetchAuthUser = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            // HttpOnly 쿠키는 JavaScript에서 직접 값을 읽을 수 없습니다.
            // 하지만 브라우저는 apiClient (withCredentials: true)를 통해 요청을 보낼 때 자동으로 쿠키를 포함합니다.
            // 따라서, 쿠키 존재 여부를 직접 확인하는 대신, /api/auth/me 엔드포인트 호출을 통해 인증 상태를 확인합니다.
            
            // ⭐ 중요: /api/auth/me 요청이 성공하면 (200 OK) -> 인증됨
            //          실패하면 (401 Unauthorized) -> 인증 안 됨 (쿠키 없거나 만료)
            const response = await apiClient.get('/api/auth/me');
            const userData = response.data; // 백엔드에서 보낸 사용자 정보 (DTO)

            if (response.status === 200 && userData && userData.username) {
                setIsAuthenticated(true);
                setUser({
                    username: userData.username,
                    nickname: userData.nickname,
                    roles: userData.roles || [],
                    profilePictureUrl: userData.profilePictureUrl || null
                });
                console.log("AuthContext: 사용자 인증 완료 (API 성공):", userData.username);
            } else {
                // 응답은 200 OK였으나 데이터가 없거나 유효하지 않은 경우 (예외적 상황)
                setIsAuthenticated(false);
                setUser(null);
                console.log("AuthContext: 사용자 데이터 가져오기 실패 (200 OK, 하지만 유효하지 않은 데이터).");
            }
        } catch (err) {
            // /api/auth/me 요청이 401 또는 다른 오류로 실패한 경우 (토큰 없거나 만료)
            console.error("AuthContext: Error fetching auth user (API 실패):", err);
            setIsAuthenticated(false);
            setUser(null);
            
            // JWT 토큰 쿠키를 삭제 (브라우저에서 직접 삭제할 수 없는 HttpOnly 쿠키이므로,
            // 백엔드 로그아웃 API 또는 Max-Age 0 쿠키 설정으로만 가능하지만,
            // 혹시 모를 경우를 대비해 클라이언트 측에서 가능한 쿠키는 삭제 시도)
            document.cookie = 'jwt_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
            document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';

            if (err.response && (err.response.status === 401 || err.response.status === 403)) {
                setError('인증 실패: 토큰이 없거나 만료되었습니다. 다시 로그인해주세요.');
            } else {
                setError('인증 중 오류 발생. 네트워크 상태를 확인해주세요.');
            }
        } finally {
            setLoading(false);
        }
    }, []);

    // 로그인 함수: LoginPage에서 호출
    // 이 함수는 백엔드로부터 받은 `response.data`를 파라미터로 받습니다.
    const login = useCallback((username, nickname, roles, profilePictureUrl) => {
        setIsAuthenticated(true);
        setUser({ username, nickname, roles, profilePictureUrl });
        setError(null);
        // ⭐⭐⭐ 전달받은 모든 파라미터가 정확히 찍히는지 확인 ⭐⭐⭐
        console.log("AuthContext: 로그인 성공! 전달받은 사용자 정보:", { username, nickname, roles, profilePictureUrl });
    }, []);

    // 로그아웃 함수: 네비게이션바 등에서 호출
    const logout = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            await apiClient.post('/api/logout'); // 백엔드 로그아웃 API 호출 (HttpOnly 쿠키 삭제는 백엔드에서 처리)

            // 클라이언트 측에서 가능한 쿠키 제거 시도 (HttpOnly 쿠키는 제거 불가하지만 안전을 위해)
            document.cookie = 'jwt_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
            document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';

            setIsAuthenticated(false);
            setUser(null);
            console.log("AuthContext: Logout successful.");
            navigate('/login'); // 로그인 페이지로 리다이렉트
            return { success: true };
        } catch (err) {
            console.error("AuthContext: Logout failed:", err);
            const errorMessage = err.response?.data?.message || '로그아웃에 실패했습니다.';
            setError(errorMessage);
            setIsAuthenticated(false);
            setUser(null);
            return { success: false, message: errorMessage };
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    // 컴포넌트 마운트 시 인증 상태 확인 함수 호출
    useEffect(() => {
        fetchAuthUser();
    }, [fetchAuthUser]); // fetchAuthUser가 변경될 때마다 재실행 (useCallback 덕분에 안정적)

    // 로딩 중일 때 표시할 UI
    if (loading) {
        return (
            <div className="flex justify-center items-center h-screen text-lg font-semibold">
                인증 정보 로딩 중...
            </div>
        );
    }

    return (
        <AuthContext.Provider value={{ isAuthenticated, user, loading, error, login, logout, setUser }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
