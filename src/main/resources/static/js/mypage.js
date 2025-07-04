document.addEventListener('DOMContentLoaded', () => {
    const messageArea = document.getElementById('message-area');

    // 메시지를 표시하는 헬퍼 함수
    function showMessage(type, message) {
        messageArea.innerHTML = ''; // 기존 메시지 초기화
        const messageDiv = document.createElement('div');
        messageDiv.classList.add(type + '-message'); // 'success-message' 또는 'error-message' 클래스 추가
        messageDiv.textContent = message;
        messageArea.appendChild(messageDiv);

        // 5초 후 메시지 사라지게 함
        setTimeout(() => {
            messageArea.innerHTML = '';
        }, 5000);
    }

    // --- 2. 사용자 정보 수정 폼 처리 ---
    const updateProfileForm = document.getElementById('updateProfileForm');
    if (updateProfileForm) {
        updateProfileForm.addEventListener('submit', async (event) => {
            event.preventDefault(); // 폼 기본 제출 동작 방지

            const formData = new FormData(updateProfileForm);
            const data = {
                // 백엔드 UserDTO에 맞게 필드 이름 지정
                nickname: formData.get('nickname'),
                birth: formData.get('birth'), // "YYYY-MM-DD" 형식으로 전달
                gender: formData.get('gender')
                // 다른 필드도 필요하다면 추가
            };

            // 만약 생년월일이 빈 문자열로 넘어오면 null 처리 (백엔드 LocalDate 파싱 오류 방지)
            if (data.birth === '') {
                data.birth = null;
            }

            try {
                const response = await fetch('/api/mypage/update', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                        // JWT가 HttpOnly 쿠키에 저장되어 있다면, 브라우저가 자동으로 쿠키를 포함하여 보냅니다.
                        // 따라서 'Authorization' 헤더에 토큰을 직접 추가할 필요는 없습니다.
                    },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const updatedUser = await response.json();
                    showMessage('success', '정보가 성공적으로 업데이트되었습니다!');
                    // 화면의 사용자 정보를 새로고침하거나 동적으로 업데이트 (예: 닉네임 필드 값 갱신)
                    // 현재는 간단히 메시지만 띄우고, 필요하다면 페이지 새로고침을 고려할 수 있습니다.
                    // location.reload(); // 사용자 정보가 많다면 페이지 새로고침이 더 간단할 수 있음
                } else {
                    const error = await response.json();
                    showMessage('error', '정보 업데이트 실패: ' + (error.message || response.statusText));
                }
            } catch (error) {
                console.error('정보 업데이트 중 오류 발생:', error);
                showMessage('error', '서버 통신 중 오류가 발생했습니다.');
            }
        });
    }

    // --- 3. 비밀번호 변경 폼 처리 ---
    const changePasswordForm = document.getElementById('changePasswordForm');
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const oldPassword = document.getElementById('oldPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmNewPassword = document.getElementById('confirmNewPassword').value;

            if (newPassword !== confirmNewPassword) {
                showMessage('error', '새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
                return;
            }
            if (newPassword.length < 4) { // 최소 길이 제한 예시
                showMessage('error', '새 비밀번호는 최소 4자 이상이어야 합니다.');
                return;
            }

            try {
                const response = await fetch('/api/mypage/change-password', {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        oldPassword: oldPassword,
                        newPassword: newPassword
                    })
                });

                if (response.ok) {
                    const message = await response.text(); // 텍스트 응답 예상
                    showMessage('success', message);
                    // 폼 초기화
                    changePasswordForm.reset();
                } else {
                    const error = await response.text(); // 텍스트 응답 예상
                    showMessage('error', '비밀번호 변경 실패: ' + error);
                }
            } catch (error) {
                console.error('비밀번호 변경 중 오류 발생:', error);
                showMessage('error', '서버 통신 중 오류가 발생했습니다.');
            }
        });
    }

    // --- 4. 회원 탈퇴 버튼 처리 ---
    const deleteAccountButton = document.getElementById('deleteAccountButton');
    if (deleteAccountButton) {
        deleteAccountButton.addEventListener('click', async () => {
            if (confirm('정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
                try {
                    const response = await fetch('/api/mypage/delete', {
                        method: 'DELETE',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    });

                    if (response.ok) {
                        const message = await response.text();
                        showMessage('success', message);
                        // 계정 삭제 성공 후 로그인 페이지로 리다이렉트
                        setTimeout(() => {
                            window.location.href = '/custom_login'; // 로그인 페이지 URL로 변경
                        }, 2000); // 2초 후 리다이렉트
                    } else {
                        const error = await response.text();
                        showMessage('error', '회원 탈퇴 실패: ' + error);
                    }
                } catch (error) {
                    console.error('회원 탈퇴 중 오류 발생:', error);
                    showMessage('error', '서버 통신 중 오류가 발생했습니다.');
                }
            }
        });
    }
});