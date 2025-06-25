import React, { useState, useEffect, useRef } from 'react';

// API 기본 URL 설정
// 실제 애플리케이션에서는 환경 변수 등으로 관리하는 것이 좋습니다.
const API_BASE_URL = 'http://localhost:8485';

// JWT 토큰을 쿠키에서 가져오는 헬퍼 함수
const getCookie = (name) => {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for(let i=0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
};

// JWT 토큰을 쿠키에서 삭제하는 헬퍼 함수
const deleteCookie = (name) => {
    document.cookie = name + '=; Max-Age=-99999999; path=/';
};

function ProfilePage() {
    const [username, setUsername] = useState('Loading...');
    const [email, setEmail] = useState('Loading...');
    const [profileImageUrl, setProfileImageUrl] = useState('/images/default_profile.png');
    const [selectedFile, setSelectedFile] = useState(null);
    const [jwtToken, setJwtToken] = useState('');
    const [message, setMessage] = useState(''); // 사용자에게 표시할 메시지
    const [isSuccessMessage, setIsSuccessMessage] = useState(false); // 메시지 타입 (성공/실패)
    const [showConfirmModal, setShowConfirmModal] = useState(false); // 확인 모달 표시 여부
    const [confirmAction, setConfirmAction] = useState(null); // 확인 모달에서 실행할 함수
    const [confirmMessage, setConfirmMessage] = useState(''); // 확인 모달 메시지

    // 컴포넌트 마운트 시 초기 데이터 로드
    useEffect(() => {
        const storedToken = getCookie('jwt_token');
        if (storedToken) {
            setJwtToken(storedToken);
            fetchUserData(storedToken);
            fetchProfileImage(storedToken);
        } else {
            setMessage('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
            setIsSuccessMessage(false);
            setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
        }
    }, []);

    // 사용자 정보 가져오기
    const fetchUserData = async (token) => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/user/me`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    setMessage('인증되지 않았거나 권한이 없습니다. 다시 로그인해주세요.');
                    setIsSuccessMessage(false);
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                }
                throw new Error('Failed to fetch user data');
            }

            const data = await response.json();
            setUsername(data.username);
            setEmail(data.email);
        } catch (error) {
            console.error('Error fetching user data:', error);
            setMessage('사용자 정보를 불러오는 데 실패했습니다.');
            setIsSuccessMessage(false);
            setUsername('불러오기 실패');
            setEmail('불러오기 실패');
        }
    };

    // 프로필 이미지 가져오기
    const fetchProfileImage = async (token) => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/files/profile-image`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.filePath) {
                    // 서버에서 파일 이름을 추출하여 URL 생성
                    const fileName = data.filePath.split('/').pop().split('\\').pop();
                    setProfileImageUrl(`${API_BASE_URL}/api/files/profile-image/${fileName}`);
                } else {
                    setProfileImageUrl('/images/default_profile.png');
                }
            } else if (response.status === 404) {
                // 이미지가 없는 경우 (404 Not Found)
                setProfileImageUrl('/images/default_profile.png');
            } else if (response.status === 401 || response.status === 403) {
                 // 인증 오류는 fetchUserData에서 처리
                console.warn('Authentication error fetching profile image. Handled by user data fetch.');
                setProfileImageUrl('/images/default_profile.png');
            } else {
                throw new Error(`Failed to fetch profile image: ${response.statusText}`);
            }
        } catch (error) {
            console.error('Error fetching profile image:', error);
            setMessage('프로필 이미지를 불러오는 데 실패했습니다.');
            setIsSuccessMessage(false);
            setProfileImageUrl('/images/default_profile.png');
        }
    };

    // 파일 선택 핸들러
    const handleFileChange = (e) => {
        setSelectedFile(e.target.files[0]);
        setMessage(''); // 메시지 초기화
    };

    // 이미지 업로드 핸들러
    const handleUpload = async () => {
        if (!selectedFile) {
            setMessage('업로드할 사진을 선택해주세요.');
            setIsSuccessMessage(false);
            return;
        }

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const response = await fetch(`${API_BASE_URL}/api/files/profile-image/upload`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${jwtToken}`
                },
                body: formData
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    setMessage('사진을 업로드할 권한이 없습니다. 다시 로그인해주세요.');
                    setIsSuccessMessage(false);
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                }
                const errorText = await response.text();
                throw new Error(`Upload failed: ${response.statusText}. Details: ${errorText}`);
            }

            setMessage('프로필 사진이 성공적으로 업로드되었습니다!');
            setIsSuccessMessage(true);
            setSelectedFile(null); // 파일 선택 초기화
            document.getElementById('imageUpload').value = ''; // input 파일 필드 초기화
            fetchProfileImage(jwtToken); // 새로운 이미지 로드
        } catch (error) {
            console.error('Error uploading image:', error);
            setMessage(`사진 업로드에 실패했습니다: ${error.message}`);
            setIsSuccessMessage(false);
        }
    };

    // 이미지 삭제 핸들러 (확인 모달 트리거)
    const confirmDelete = () => {
        setConfirmMessage('프로필 사진을 정말 삭제하시겠습니까?');
        setConfirmAction(() => handleDelete); // 클로저로 handleDelete 함수 전달
        setShowConfirmModal(true);
    };

    // 실제 이미지 삭제 로직
    const handleDelete = async () => {
        setShowConfirmModal(false); // 확인 모달 닫기

        try {
            const response = await fetch(`${API_BASE_URL}/api/files/profile-image`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${jwtToken}`
                }
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    setMessage('사진을 삭제할 권한이 없습니다. 다시 로그인해주세요.');
                    setIsSuccessMessage(false);
                    setTimeout(() => { window.location.href = '/custom_login'; }, 2000);
                }
                const errorText = await response.text();
                throw new Error(`Deletion failed: ${response.statusText}. Details: ${errorText}`);
            }

            setMessage('프로필 사진이 성공적으로 삭제되었습니다!');
            setIsSuccessMessage(true);
            setProfileImageUrl('/images/default_profile.png'); // 기본 이미지로 되돌리기
        } catch (error) {
            console.error('Error deleting image:', error);
            setMessage(`사진 삭제에 실패했습니다: ${error.message}`);
            setIsSuccessMessage(false);
        }
    };

    // 로그아웃 핸들러 (확인 모달 트리거)
    const confirmLogout = () => {
        setConfirmMessage('정말로 로그아웃 하시겠습니까?');
        setConfirmAction(() => handleLogout);
        setShowConfirmModal(true);
    };

    // 실제 로그아웃 로직
    const handleLogout = async () => {
        setShowConfirmModal(false); // 확인 모달 닫기
        try {
            // 서버에 로그아웃 요청 (JWT 토큰 무효화 등)
            const response = await fetch(`${API_BASE_URL}/api/logout`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${jwtToken}`
                }
            });

            if (response.ok || response.status === 401) { // 401이더라도 클라이언트에서는 로그아웃 처리
                setMessage('로그아웃 되었습니다.');
                setIsSuccessMessage(true);
            } else {
                setMessage('로그아웃 중 오류가 발생했습니다.');
                setIsSuccessMessage(false);
            }
        } catch (error) {
            console.error('Logout error:', error);
            setMessage('로그아웃 중 네트워크 오류가 발생했습니다.');
            setIsSuccessMessage(false);
        } finally {
            deleteCookie('jwt_token'); // 클라이언트 측 쿠키 삭제
            setJwtToken(''); // 상태 초기화
            setTimeout(() => { window.location.href = '/custom_login'; }, 1000); // 로그인 페이지로 리디렉션
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center font-sans py-10">
            {/* Message Display */}
            {message && (
                <div className={`fixed top-4 right-4 z-50 p-4 rounded-md shadow-lg ${isSuccessMessage ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {message}
                </div>
            )}

            <div className="bg-white p-8 rounded-lg shadow-xl max-w-lg w-full text-center">
                <h1 className="text-3xl font-bold text-gray-800 mb-8">프로필 사진 관리</h1>

                <div className="profile-section mb-8">
                    <h2 className="text-2xl font-semibold text-gray-700 mb-4">내 프로필</h2>
                    <p className="text-lg text-gray-600 mb-2">
                        사용자 아이디: <span className="font-medium text-gray-800">{username}</span>
                    </p>
                    <p className="text-lg text-gray-600 mb-6">
                        이메일: <span className="font-medium text-gray-800">{email}</span>
                    </p>

                    <h3 className="text-xl font-semibold text-gray-700 mb-4">프로필 사진</h3>
                    <div className="profile-picture-area flex flex-col items-center mb-6">
                        <img
                            id="profileImage"
                            src={profileImageUrl}
                            alt="프로필 이미지"
                            className="w-40 h-40 rounded-full object-cover border-4 border-blue-400 shadow-md transition-transform transform hover:scale-105"
                            onError={(e) => { e.target.onerror = null; e.target.src = '/images/default_profile.png'; }}
                        />
                        {profileImageUrl === '/images/default_profile.png' && (
                            <p id="noPictureMessage" className="text-gray-500 mt-2">현재 프로필 사진이 없습니다.</p>
                        )}
                    </div>

                    <div className="picture-actions flex flex-col sm:flex-row justify-center items-center gap-4">
                        <input
                            type="file"
                            id="imageUpload"
                            accept="image/*"
                            className="block w-full sm:w-auto text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                            onChange={handleFileChange}
                        />
                        <div className="flex gap-4 w-full sm:w-auto justify-center">
                            <button
                                id="uploadButton"
                                className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out flex-1"
                                onClick={handleUpload}
                                disabled={!selectedFile}
                            >
                                사진 업로드
                            </button>
                            <button
                                id="deleteButton"
                                className={`bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out flex-1 ${profileImageUrl === '/images/default_profile.png' ? 'opacity-50 cursor-not-allowed' : ''}`}
                                onClick={confirmDelete}
                                disabled={profileImageUrl === '/images/default_profile.png'}
                            >
                                사진 삭제
                            </button>
                        </div>
                    </div>
                </div>

                <button
                    id="logoutButton"
                    className="logout-button bg-gray-700 hover:bg-gray-800 text-white font-bold py-3 px-8 rounded-md transition duration-150 ease-in-out mt-8 w-full"
                    onClick={confirmLogout}
                >
                    로그아웃
                </button>
            </div>

            {/* Confirmation Modal */}
            {showConfirmModal && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-sm mx-auto relative text-center">
                        <h5 className="text-xl font-bold text-gray-800 mb-5">확인</h5>
                        <p className="text-gray-600 mb-6">{confirmMessage}</p>
                        <div className="flex justify-center gap-4">
                            <button
                                className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={confirmAction}
                            >
                                예
                            </button>
                            <button
                                className="bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={() => setShowConfirmModal(false)}
                            >
                                아니오
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ProfilePage;
