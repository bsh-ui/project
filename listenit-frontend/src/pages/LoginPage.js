import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import apiClient from '../api/apiClient'; // apiClient 임포트
import { useAuth } from '../context/AuthContext'; // AuthContext 임포트
// import { loginUser } from '../services/api'; // ⭐ loginUser 함수 더 이상 직접 사용 안함 ⭐
import '../styles/Login.css'; // 별도의 CSS 파일

// 함수 이름은 App.js의 라우트와 일치하도록 Login 대신 LoginPage로 유지하는 것이 좋습니다.
function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [rememberMe, setRememberMe] = useState(false);
    const [loginMessage, setLoginMessage] = useState(''); // 로그인 메시지 (성공/실패)
    const [isSuccessLoginMessage, setIsSuccessLoginMessage] = useState(false); // 메시지 타입
    const messageTimeoutRef = useRef(null); // 메시지 타이머 ref

    const navigate = useNavigate();
    const location = useLocation();

    const { login: authLogin, isAuthenticated } = useAuth(); // AuthContext의 login 함수와 isAuthenticated 상태 가져오기

    // 전역 메시지 표시 함수
    const showTemporaryMessage = (msg, isSuccess = false) => {
        if (messageTimeoutRef.current) {
            clearTimeout(messageTimeoutRef.current);
        }
        setLoginMessage(msg);
        setIsSuccessLoginMessage(isSuccess);
        messageTimeoutRef.current = setTimeout(() => {
            setLoginMessage('');
        }, 3000); // 3초 후 메시지 사라짐
    };

    // 컴포넌트 마운트 시
    useEffect(() => {
        // 이미 로그인되어 있다면 메인 페이지로 리다이렉트
        // AuthContext의 isAuthenticated 상태가 true면 바로 리다이렉트
        if (isAuthenticated) {
            // location.state?.from을 사용하여 로그인 전 페이지로 돌아갈 수 있도록 함
            const from = location.state?.from?.pathname || '/main';
            navigate(from, { replace: true });
            return;
        }

        // URL 쿼리 파라미터에서 에러/로그아웃 메시지 처리
        const params = new URLSearchParams(location.search);
        if (params.has('error')) {
            showTemporaryMessage('로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.', false);
        } else if (params.has('logout')) {
            showTemporaryMessage('로그아웃 되었습니다.', true);
        }

        // 아이디 저장 (Remember Me) 로직 불러오기
        const storedUsername = localStorage.getItem('savedUsername');
        const rememberMeChecked = localStorage.getItem('rememberMeChecked');

        if (storedUsername) {
            setUsername(storedUsername);
        }
        if (rememberMeChecked === 'true') {
            setRememberMe(true);
        }

        // 컴포넌트 언마운트 시 타이머 정리
        return () => {
            if (messageTimeoutRef.current) {
                clearTimeout(messageTimeoutRef.current);
            }
        };
    }, [location.search, navigate, isAuthenticated, location.state]); // location.state 추가

    const handleLoginSubmit = async (event) => {
        // ⭐⭐⭐ 이 로그가 브라우저 개발자 도구 콘솔에 나타나는지 확인해주세요! ⭐⭐⭐
        console.log("DEBUG: handleLoginSubmit 함수 시작됨."); 
        event.preventDefault(); 
        console.log("DEBUG: 기본 폼 제출 방지됨."); // ⭐⭐ 이 로그도 나타나는지 확인! ⭐⭐
        console.log("DEBUG: 전송될 username:", username);
        console.log("DEBUG: 전송될 password:", password); 
        showTemporaryMessage(''); // 이전 메시지 초기화

        // Remember Me 로직 업데이트
        if (rememberMe) {
            localStorage.setItem('savedUsername', username);
            localStorage.setItem('rememberMeChecked', 'true');
        } else {
            localStorage.removeItem('savedUsername');
            localStorage.removeItem('rememberMeChecked');
        }

        try {
            // apiClient.post를 직접 사용하여 백엔드 로그인 API 호출
              console.log("DEBUG: /api/login 요청 시작 전."); // 요청 시작

            const response = await apiClient.post('/api/login', {
                username: username,
                password: password,
            });
            console.log("DEBUG: /api/login API 응답 전체:", response);
            console.log("DEBUG: /api/login API 응답 데이터:", response.data);
            if (response.data && response.data.success) {
                authLogin(
                    response.data.username,
                    response.data.nickname,
                    response.data.roles,
                    response.data.profilePictureUrl
                );
                showTemporaryMessage('로그인 성공!', true);
                const from = location.state?.from?.pathname || '/main';
                navigate(from, { replace: true });
            } else {
                showTemporaryMessage(response.data.message || '로그인에 실패했습니다. 다시 시도해주세요.', false);
            }
        } catch (error) {
            console.error('로그인 요청 중 오류 발생:', error);
            const errorMessage = error.response?.data?.message || '로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.';
            showTemporaryMessage(errorMessage, false);
        }
    };

    return (
        <div className="font-sans bg-gray-100 flex justify-center items-center min-h-screen p-4">
            {/* Login Message Display */}
            {loginMessage && (
                <div className={`fixed top-4 right-4 z-50 p-4 rounded-md shadow-lg ${isSuccessLoginMessage ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {loginMessage}
                </div>
            )}

            <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-sm text-center">
                <h1 className="text-3xl font-bold text-gray-800 mb-6">로그인</h1>

                <div className="login-form-container p-5 border border-gray-200 rounded-md bg-gray-50 mb-6">
                    {/* form 태그에 action 속성이 없어야 합니다! ⭐⭐ */}
                    <form onSubmit={handleLoginSubmit}>
                        <div className="mb-4 text-left">
                            <label htmlFor="username" className="block text-gray-700 text-sm font-bold mb-2">아이디:</label>
                            <input
                                type="text"
                                id="username"
                                name="username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div className="mb-4 text-left">
                            <label htmlFor="password" className="block text-gray-700 text-sm font-bold mb-2">비밀번호:</label>
                            <input
                                type="password"
                                id="password"
                                name="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div className="flex items-center mb-6">
                            <input
                                type="checkbox"
                                id="rememberMe"
                                name="rememberMe"
                                checked={rememberMe}
                                onChange={(e) => setRememberMe(e.target.checked)}
                                className="form-checkbox h-4 w-4 text-blue-600 rounded"
                            />
                            <label htmlFor="rememberMe" className="ml-2 text-gray-700 text-sm">아이디 저장</label>
                        </div>
                        <button
                            type="submit"
                            className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transition duration-150 ease-in-out w-full"
                        >
                            로그인
                        </button>
                    </form>
                </div>

                <div className="social-login space-y-3 mb-6">
                    {/* OAuth2 로그인 링크는 여전히 백엔드 URL로 직접 연결되어야 합니다. */}
                    <a href={`${apiClient.defaults.baseURL}/oauth2/authorization/google`} className="block py-2 px-4 rounded-md font-bold transition duration-150 ease-in-out bg-red-600 hover:bg-red-700 text-white">Google 로그인</a>
                    <a href={`${apiClient.defaults.baseURL}/oauth2/authorization/naver`} className="block py-2 px-4 rounded-md font-bold transition duration-150 ease-in-out bg-green-500 hover:bg-green-600 text-white">Naver 로그인</a>
                    <a href={`${apiClient.defaults.baseURL}/oauth2/authorization/kakao`} className="block py-2 px-4 rounded-md font-bold transition duration-150 ease-in-out bg-yellow-400 hover:bg-yellow-500 text-gray-900">Kakao 로그인</a>
                </div>

                <div className="signup-link text-center">
                    <p className="text-gray-600 mb-2">계정이 없으신가요? <Link to="/signup" className="text-blue-600 hover:underline font-medium">회원가입</Link></p>
                    <Link to="/" className="text-gray-500 hover:underline text-sm">뒤로가기</Link>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;
