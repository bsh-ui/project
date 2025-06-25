// src/context/AuthContext.js
import React, { createContext, useState, useEffect, useContext } from 'react';
import { login as apiLogin, logout as apiLogout, getAuthenticatedUser as apiGetAuthenticatedUser } from '../services/authService'; // authService에서 인증 관련 함수 임포트
import { useNavigate } from 'react-router-dom';

// 1. AuthContext 생성
export const AuthContext = createContext();

// 2. AuthProvider 컴포넌트 정의
// 이 컴포넌트는 자식 컴포넌트들에게 인증 상태와 함수를 제공합니다.
export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false); // 사용자의 인증 상태
    const [user, setUser] = useState(null); // 로그인된 사용자 정보 (UserDTO)
    const [loading, setLoading] = useState(true); // 초기 인증 상태 로딩 여부
    const [error, setError] = useState(null); // 인증 관련 에러 메시지 (alert() 대체)
    const navigate = useNavigate();

    // 3. 컴포넌트 마운트 시 인증 상태를 확인하는 useEffect 훅
    useEffect(() => {
        const checkAuthStatus = async () => {
            setLoading(true); // 인증 확인 시작 시 로딩 상태 true로 설정
            try {
                // localStorage에서 JWT 토큰 존재 여부 확인
                const token = localStorage.getItem('jwt_token');
                if (token) {
                    // 토큰이 있다면 백엔드를 통해 사용자 정보를 가져와 유효성 검증
                    const authenticatedUser = await apiGetAuthenticatedUser(); // authService의 getAuthenticatedUser 호출
                    if (authenticatedUser) {
                        setIsAuthenticated(true);
                        setUser(authenticatedUser);
                        console.log("인증 성공: 사용자 정보 로드됨", authenticatedUser);
                    } else {
                        // 토큰은 있지만 유효하지 않은 경우
                        setIsAuthenticated(false);
                        setUser(null);
                        localStorage.removeItem('jwt_token'); // 유효하지 않은 토큰 제거
                        localStorage.removeItem('user_info'); // 사용자 정보도 제거
                        console.log("인증 실패: 유효하지 않은 토큰");
                    }
                } else {
                    // 토큰이 없는 경우
                    setIsAuthenticated(false);
                    setUser(null);
                    console.log("인증 실패: 토큰 없음");
                }
            } catch (err) {
                // API 호출 중 오류 발생 (네트워크, 서버 오류 등)
                console.error('인증 상태 확인 실패:', err);
                setIsAuthenticated(false);
                setUser(null);
                // ⭐ 에러를 사용자에게 직접 보여주기보다, error 상태를 업데이트하여 UI에 표시하도록 처리 ⭐
                setError(err.message || '인증 상태 확인 중 오류가 발생했습니다.');
                localStorage.removeItem('jwt_token'); // 오류 발생 시 토큰 및 정보 삭제
                localStorage.removeItem('user_info');
            } finally {
                setLoading(false); // 로딩 완료
            }
        };

        checkAuthStatus();
    }, []); // 빈 의존성 배열: 컴포넌트 마운트 시 한 번만 실행

    // 4. 로그인 함수
    const login = async (username, password) => {
        setLoading(true); // 로그인 시도 시 로딩 시작
        setError(null); // 이전 에러 메시지 초기화
        try {
            const result = await apiLogin(username, password); // authService의 login 함수 호출
            if (result.success) {
                setIsAuthenticated(true);
                setUser(result.user);
                console.log("로그인 성공:", result.user);
                navigate('/'); // 로그인 성공 시 메인 페이지(홈)으로 이동
                return { success: true };
            } else {
                setError(result.message || '로그인에 실패했습니다.'); // 로그인 실패 메시지 설정
                return { success: false, message: result.message || '로그인에 실패했습니다.' };
            }
        } catch (err) {
            console.error('로그인 중 오류:', err);
            setError(err.message || '로그인 중 오류가 발생했습니다.'); // 에러 메시지 설정
            setIsAuthenticated(false);
            setUser(null);
            throw err; // 에러를 호출자에게 다시 던짐
        } finally {
            setLoading(false); // 로그인 시도 완료 시 로딩 종료
        }
    };

    // 5. 로그아웃 함수
    const logout = async () => {
        setLoading(true); // 로그아웃 시도 시 로딩 시작
        setError(null); // 이전 에러 메시지 초기화
        try {
            await apiLogout(); // authService의 logout 함수 호출
            setIsAuthenticated(false);
            setUser(null);
            localStorage.removeItem('jwt_token'); // localStorage에서 토큰 제거
            localStorage.removeItem('user_info'); // localStorage에서 사용자 정보 제거
            console.log("로그아웃 성공");
            navigate('/login'); // 로그아웃 성공 시 로그인 페이지로 이동
            return { success: true };
        } catch (err) {
            console.error('로그아웃 실패:', err);
            setError(err.message || '로그아웃 중 오류가 발생했습니다.'); // 에러 메시지 설정
            // ⭐ alert() 대신 error 상태를 사용하므로 이 줄은 제거
            return { success: false, message: err.message || '로그아웃에 실패했습니다.' };
        } finally {
            setLoading(false); // 로그아웃 시도 완료 시 로딩 종료
        }
    };

    // 6. Context 값으로 제공할 객체
    const authContextValue = {
        isAuthenticated,
        user,
        loading, // 인증 로딩 상태
        error,   // 에러 상태 (UI에서 활용 가능)
        login,
        logout,
        setUser // ⭐ MyPage 등에서 사용자 정보 업데이트 후 Context 상태를 직접 갱신할 수 있도록 추가
    };

    // 로딩 중일 때 표시할 UI
    if (loading) {
        return (
            <div className="flex justify-center items-center h-screen text-lg font-semibold">
                인증 정보 로딩 중...
            </div>
        );
    }

    return (
        // value prop을 통해 자식 컴포넌트들에게 인증 상태와 함수를 제공합니다.
        <AuthContext.Provider value={authContextValue}>
            {children}
        </AuthContext.Provider>
    );
};

// 7. useAuth 훅 정의
// 이 훅을 사용하여 어떤 컴포넌트에서든 AuthContext의 값에 쉽게 접근할 수 있습니다.
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) { // !context 대신 undefined 체크가 좀 더 명확함
        throw new Error('useAuth는 AuthProvider 내에서 사용되어야 합니다.');
    }
    return context;
};
