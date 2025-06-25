import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import '../ForgotPassword.css';
import apiClient from '../api/apiClient'; // apiClient 임포트

function ForgotPassword() {
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');
    const [isSuccess, setIsSuccess] = useState(false);
    // 선택 사항: 로딩 상태를 추가하여 버튼 비활성화 등에 활용할 수 있습니다.
    const [isLoading, setIsLoading] = useState(false); 

    const handleSubmit = async (event) => {
        event.preventDefault(); // 폼 기본 제출 동작 방지
        setMessage(''); // 이전 메시지 초기화
        setIsLoading(true); // 로딩 시작

        try {
            // ⭐ 핵심 수정: apiClient.post 사용법
            // axios (apiClient)는 두 번째 인자로 요청 본문(body) 데이터를 바로 객체 형태로 받습니다.
            // JSON.stringify, method, headers는 axios가 내부적으로 처리합니다.
            const response = await apiClient.post('/api/auth/reset-password-request', { email: email });

            // ⭐ 핵심 수정: axios 응답 처리 방식
            // axios는 응답 데이터가 response.data에 JSON 파싱된 형태로 들어옵니다.
            // 2xx 응답 (성공)일 경우 이 try 블록이 실행됩니다.
            setMessage(response.data.message || '비밀번호 재설정 링크가 이메일로 전송되었습니다. 이메일을 확인해주세요.');
            setIsSuccess(true);
        } catch (error) {
            // ⭐ 핵심 수정: axios 에러 처리 방식
            // axios 에러는 error.response에 서버 응답이 담겨 있습니다.
            console.error('비밀번호 재설정 요청 실패:', error.response ? error.response.data : error.message);

            if (error.response && error.response.data && error.response.data.message) {
                // 서버에서 특정 에러 메시지를 보낸 경우
                setMessage(error.response.data.message);
            } else if (error.request) {
                // 요청은 보냈으나 응답을 받지 못함 (네트워크 오류 등)
                setMessage('네트워크 오류 또는 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
            } else {
                setMessage('비밀번호 재설정 요청 중 알 수 없는 오류가 발생했습니다.');
            }
            setIsSuccess(false);
        } finally {
            setIsLoading(false); // 로딩 종료
        }
    };

    return (
        <div className="container">
            <h1>비밀번호 찾기</h1>
            <p>가입 시 사용한 이메일 주소를 입력해주세요. 비밀번호 재설정 링크를 보내드립니다.</p>

            <form onSubmit={handleSubmit} id="forgotPasswordForm">
                <div>
                    <label htmlFor="email">이메일:</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" disabled={isLoading}> {/* 로딩 중 버튼 비활성화 */}
                    {isLoading ? '전송 중...' : '재설정 링크 보내기'}
                </button>
            </form>

            {message && (
                <div id="message-area" className={`message-area ${isSuccess ? 'success-message' : 'error-message'}`}>
                    {message}
                </div>
            )}

            <Link to="/custom_login" className="back-link">로그인 페이지로 돌아가기</Link>
        </div>
    );
}

export default ForgotPassword;
