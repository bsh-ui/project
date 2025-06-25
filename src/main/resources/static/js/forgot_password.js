document.addEventListener('DOMContentLoaded', () => {
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');
    const messageArea = document.getElementById('message-area');

    // 메시지를 표시하는 헬퍼 함수 (mypage.js와 유사)
    function showMessage(type, message) {
        messageArea.innerHTML = '';
        const messageDiv = document.createElement('div');
        messageDiv.classList.add(type + '-message'); // 'success-message' 또는 'error-message' 클래스 추가
        messageDiv.textContent = message;
        messageArea.appendChild(messageDiv);
    }

    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', async (event) => {
            event.preventDefault(); // 폼 기본 제출 동작 방지

            const email = document.getElementById('email').value;

            try {
                const response = await fetch('/api/forgot-password/send-email', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email: email }) // JSON 형식으로 이메일 전송
                });

                if (response.ok) {
                    const data = await response.text(); // 백엔드에서 텍스트 응답을 기대
                    showMessage('success', data);
                    forgotPasswordForm.reset(); // 폼 초기화
                } else {
                    const error = await response.text(); // 백엔드에서 에러 시 텍스트 응답을 기대
                    showMessage('error', '이메일 발송 실패: ' + error);
                }
            } catch (error) {
                console.error('비밀번호 재설정 이메일 요청 중 오류 발생:', error);
                showMessage('error', '서버 통신 중 오류가 발생했습니다.');
            }
        });
    }
});