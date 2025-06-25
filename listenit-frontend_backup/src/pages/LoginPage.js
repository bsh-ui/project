import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import apiClient from '../api/apiClient'; // apiClient 임포트
import { useAuth } from '../context/AuthContext'; // AuthContext 임포트
import { loginUser } from '../services/api'; // services/api에서 loginUser 함수 임포트
import '../Login.css'; // 별도의 CSS 파일

function Login() {
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

    useEffect(() => {
        // 이미 로그인되어 있다면 메인 페이지로 리다이렉트
        if (isAuthenticated) {
            navigate('/main');
            return;
        }

        const params = new URLSearchParams(location.search);
        if (params.has('error')) {
            showTemporaryMessage('로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.', false);
        } else if (params.has('logout')) {
            showTemporaryMessage('로그아웃 되었습니다.', true);
        }

        const storedUsername = localStorage.getItem('savedUsername');
        const rememberMeChecked = localStorage.getItem('rememberMeChecked');

        if (storedUsername) {
            setUsername(storedUsername);
        }
        if (rememberMeChecked === 'true') {
            setRememberMe(true);
        }

        return () => {
            if (messageTimeoutRef.current) {
                clearTimeout(messageTimeoutRef.current);
            }
        };
    }, [location.search, navigate, isAuthenticated]);

    const handleLoginSubmit = async (event) => {
        event.preventDefault();
        showTemporaryMessage(''); // 이전 메시지 초기화

        try {
            // services/api의 loginUser 함수 사용
            const response = await loginUser({ username, password });

            if (response.data && response.data.accessToken) {
                // 로그인 성공 처리
                authLogin(response.data.user, response.data.accessToken); // AuthContext에 사용자 정보 및 토큰 저장

                if (rememberMe) {
                    localStorage.setItem('savedUsername', username);
                    localStorage.setItem('rememberMeChecked', 'true');
                } else {
                    localStorage.removeItem('savedUsername');
                    localStorage.removeItem('rememberMeChecked');
                }
                showTemporaryMessage('로그인 성공!', true);
                navigate('/main'); // 메인 페이지로 이동
            } else {
                showTemporaryMessage('로그인에 실패했습니다. 다시 시도해주세요.', false);
                console.error('Login failed with unexpected response:', response);
            }
        } catch (error) {
            console.error('로그인 요청 중 오류 발생:', error);
            if (error.response && error.response.data && error.response.data.message) {
                showTemporaryMessage(`로그인 실패: ${error.response.data.message}`, false);
            } else {
                showTemporaryMessage('네트워크 오류 또는 서버에 연결할 수 없습니다.', false);
            }
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

export default Login;
