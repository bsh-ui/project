document.addEventListener('DOMContentLoaded', () => {
    const profileImage = document.getElementById('profileImage');
    const imageUpload = document.getElementById('imageUpload');
    const uploadButton = document.getElementById('uploadButton');
    const deleteButton = document.getElementById('deleteButton');
    const messageDisplay = document.getElementById('message');
    const usernameDisplay = document.getElementById('username');
    const emailDisplay = document.getElementById('email');
    const noPictureMessage = document.getElementById('noPictureMessage');
    const logoutButton = document.getElementById('logoutButton');

    const API_BASE_URL = 'http://localhost:8485/api'; // 백엔드 API 기본 URL (포트 맞춰주세요!)
    const TOKEN_KEY = 'jwt_token'; // JWT 토큰을 저장할 localStorage 키

    // --- 유틸리티 함수 ---
    function showMessage(msg, type = 'info') {
        messageDisplay.textContent = msg;
        messageDisplay.className = `status-message ${type}`;
    }

    function clearMessage() {
        messageDisplay.textContent = '';
        messageDisplay.className = 'status-message';
    }

    function getAuthHeaders() {
        const token = localStorage.getItem(TOKEN_KEY);
        if (!token) {
            showMessage('로그인 정보가 없습니다. 다시 로그인해주세요.', 'error');
            //setTimeout(() => window.location.href = '/main', 2000); // 로그인 페이지로 리디렉션 (main이 있다고 가정)
            return null;
        }
        return {
            'Authorization': `Bearer ${token}`
        };
    }

    // --- 사용자 정보 및 프로필 사진 로드 함수 ---
    async function loadUserProfile() {
        clearMessage();
        const headers = getAuthHeaders();
        if (!headers) return;

        try {
            // 1. 사용자 정보 가져오기 (예시: /api/user/me 엔드포인트가 있다고 가정)
            const userResponse = await fetch(`${API_BASE_URL}/user/me`, {
                method: 'GET',
                headers: { ...headers }
            });

            if (userResponse.status === 401) {
                showMessage('세션이 만료되었습니다. 다시 로그인해주세요.', 'error');
                localStorage.removeItem(TOKEN_KEY);
                setTimeout(() => window.location.href = '/main', 2000);
                return;
            }

            if (!userResponse.ok) {
                const errorData = await userResponse.json();
                throw new Error(errorData.message || '사용자 정보 로드 실패');
            }

            const userData = await userResponse.json();
            usernameDisplay.textContent = userData.username;
            emailDisplay.textContent = userData.email;

            // 2. 프로필 사진 업데이트
            const pictureUrl = userData.picture; // UserDTO의 picture 필드
            if (pictureUrl) {
                profileImage.src = pictureUrl; // 예: /profile-images/uuid.jpg
                profileImage.style.display = 'block';
                noPictureMessage.style.display = 'none';
                deleteButton.style.display = 'inline-block'; // 사진이 있으면 삭제 버튼 보이기
            } else {
                // 프로필 사진이 없는 경우 기본 이미지 또는 메시지 표시
                profileImage.src = '/images/default_profile.png'; // 기본 이미지
                profileImage.style.display = 'block';
                noPictureMessage.style.display = 'block';
                deleteButton.style.display = 'none'; // 사진이 없으면 삭제 버튼 숨기기
            }

        } catch (error) {
            console.error('프로필 로드 오류:', error);
            showMessage(`프로필 로드 실패: ${error.message}`, 'error');
            profileImage.src = '/images/error_profile.png'; // 오류 시 기본 이미지
            profileImage.style.display = 'block';
            noPictureMessage.style.display = 'none';
        }
    }

    // --- 이벤트 리스너 ---

    // 사진 업로드 버튼 클릭 이벤트
    uploadButton.addEventListener('click', async () => {
        clearMessage();
        const file = imageUpload.files[0];
        if (!file) {
            showMessage('업로드할 파일을 선택해주세요.', 'info');
            return;
        }

        const headers = getAuthHeaders();
        if (!headers) return;

        const formData = new FormData();
        formData.append('file', file); // 'file' 키는 백엔드의 @RequestParam("file")과 일치해야 함

        try {
            showMessage('파일 업로드 중...', 'info');
            const response = await fetch(`${API_BASE_URL}/profile/upload-picture`, {
                method: 'POST',
                headers: {
                    'Authorization': headers['Authorization'] // Content-Type은 FormData가 자동으로 설정
                },
                body: formData
            });

            if (response.status === 401) {
                showMessage('세션이 만료되었습니다. 다시 로그인해주세요.', 'error');
                localStorage.removeItem(TOKEN_KEY);
                setTimeout(() => window.location.href = '/main', 2000);
                return;
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '파일 업로드 실패');
            }

            const result = await response.json();
            showMessage(result.message, 'success');
            // 업로드 성공 후 프로필 사진 새로고침
            loadUserProfile();

        } catch (error) {
            console.error('업로드 오류:', error);
            showMessage(`파일 업로드 실패: ${error.message}`, 'error');
        }
    });

    // 사진 삭제 버튼 클릭 이벤트
    deleteButton.addEventListener('click', async () => {
        clearMessage();
        if (!confirm('정말로 프로필 사진을 삭제하시겠습니까?')) {
            return;
        }

        const headers = getAuthHeaders();
        if (!headers) return;

        try {
            showMessage('사진 삭제 중...', 'info');
            const response = await fetch(`${API_BASE_URL}/profile/delete-picture`, {
                method: 'DELETE',
                headers: { ...headers }
            });

            if (response.status === 401) {
                showMessage('세션이 만료되었습니다. 다시 로그인해주세요.', 'error');
                localStorage.removeItem(TOKEN_KEY);
                setTimeout(() => window.location.href = '/main', 2000);
                return;
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '사진 삭제 실패');
            }

            const result = await response.json();
            showMessage(result.message, 'success');
            // 삭제 성공 후 프로필 사진 새로고침
            loadUserProfile();

        } catch (error) {
            console.error('삭제 오류:', error);
            showMessage(`사진 삭제 실패: ${error.message}`, 'error');
        }
    });

    // 로그아웃 버튼 클릭 이벤트
    logoutButton.addEventListener('click', () => {
        localStorage.removeItem(TOKEN_KEY); // JWT 토큰 삭제
        // 필요하다면 백엔드 로그아웃 API 호출 (블랙리스트 추가 등)
        // fetch(`${API_BASE_URL}/auth/logout`, { method: 'POST', headers: getAuthHeaders() })
        //     .then(res => { console.log('로그아웃 처리됨'); })
        //     .catch(err => { console.error('로그아웃 오류:', err); });
        
        showMessage('로그아웃 되었습니다.', 'info');
        setTimeout(() => window.location.href = '/main', 1000); // 로그인 페이지로 이동
    });


    // 페이지 로드 시 프로필 정보 및 사진 로드
    loadUserProfile();
});