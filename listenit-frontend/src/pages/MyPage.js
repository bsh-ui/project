import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient'; // apiClient 임포트
import { useAuth } from '../context/AuthContext'; // AuthContext 임포트
import '../styles/MyPage.css'; // MyPage CSS 파일 경로 (src/styles/MyPage.css)

function MyPage() {
    // ⭐ 모든 useState 훅을 컴포넌트 최상단에 선언합니다.
    const [user, setUser] = useState(null); // 사용자 정보
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null); // 전역 오류 메시지
    const [message, setMessage] = useState(''); // 전역 성공 메시지

    // 프로필 수정 폼 상태
    const [nickname, setNickname] = useState('');
    const [birth, setBirth] = useState('');
    const [gender, setGender] = useState('');

    // 비밀번호 변경 폼 상태
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');

    // 비밀번호 변경 에러 메시지 (로컬 스코프)
    const [passwordChangeError, setPasswordChangeError] = useState('');

    // 범용 확인 모달 상태
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [confirmMessage, setConfirmMessage] = useState('');
    const [confirmAction, setConfirmAction] = useState(null); // 모달 '예' 클릭 시 실행될 함수

    // 회원 탈퇴 확인 모달 (분리된 상태) - 이전 버전에서는 이 부분이 없었습니다. (새로 추가되었거나 기존 코드에 숨겨져 있었을 수 있음)
    // 현재는 이 부분을 MyPage.js 에 포함하지 않는 것이 좋습니다.
    // const [showWithdrawConfirm, setShowWithdrawConfirm] = useState(false);
    // const [withdrawMessage, setWithdrawMessage] = useState('');

    // ⭐ isSuccess 상태를 다른 useState 훅들과 함께 최상단에 선언합니다.
    const [isSuccess, setIsSuccess] = useState(false);


    const navigate = useNavigate();
    const { logout: authLogout, user: authUser, isAuthenticated } = useAuth(); // AuthContext에서 로그아웃 함수와 사용자 정보, 인증 상태 가져오기

    // 임시 메시지 표시 함수 (성공/오류)
    const showTemporaryMessage = (msg, isError = false) => {
        if (isError) {
            setError(msg);
            setMessage(''); // 성공 메시지는 지웁니다.
        } else {
            setMessage(msg);
            setError(null); // 오류 메시지는 지웁니다.
        }
        setTimeout(() => {
            setMessage('');
            setError(null);
        }, 3000); // 3초 후 메시지 사라짐
    };

    // 사용자 정보 로드 및 인증 확인 useEffect
    useEffect(() => {
        // 인증되지 않았다면 로그인 페이지로 리다이렉트
        if (!isAuthenticated && !loading) { // loading 상태를 함께 확인하여 초기 렌더링 시 불필요한 리다이렉트 방지
            navigate('/login');
            return;
        }

        const fetchUserProfile = async () => {
            if (!authUser) { // authUser가 없으면 API 호출하지 않음
                setLoading(false);
                setError('사용자 정보를 찾을 수 없습니다.');
                return;
            }

            try {
                // `/api/user/me` 엔드포인트 호출 (인증된 사용자 정보 가져오기)
                const response = await apiClient.get('/api/user/me');
                const userData = response.data;
                setUser(userData);
                setNickname(userData.nickname || '');
                setBirth(userData.birth || ''); // 백엔드에서 제공하는 날짜 형식에 맞게 설정
                setGender(userData.gender || 'N');
            } catch (err) {
                console.error('사용자 정보 로드 실패:', err);
                setError('사용자 정보를 불러오는 데 실패했습니다.');
                // 401 또는 403 에러 발생 시 apiClient 인터셉터에서 처리되므로 여기서는 추가적인 리다이렉트 불필요
            } finally {
                setLoading(false);
            }
        };

        if (isAuthenticated) { // 로그인 상태일 때만 사용자 정보 fetch
            fetchUserProfile();
        } else {
            setLoading(false); // 로그인되지 않은 상태면 로딩 종료
        }
    }, [isAuthenticated, navigate, authUser, loading]); // 종속성 배열에 모든 관련 변수 추가

    // 프로필 정보 수정 핸들러
    const handleProfileUpdate = async (event) => {
        event.preventDefault();
        setMessage(''); // 메시지 초기화
        setError(null);

        // 간단한 클라이언트 측 유효성 검사
        if (!nickname.trim()) {
            showTemporaryMessage('닉네임은 필수 항목입니다.', true);
            return;
        }
        if (!birth) {
            showTemporaryMessage('생년월일은 필수 항목입니다.', true);
            return;
        }
        if (!gender || gender === 'N') {
            showTemporaryMessage('성별을 선택해주세요.', true);
            return;
        }

        try {
            const response = await apiClient.put('/api/user/update', {
                nickname,
                birth,
                gender
            });
            showTemporaryMessage(response.data.message || '프로필이 성공적으로 업데이트되었습니다.', false);
            // AuthContext의 사용자 정보도 업데이트
            // authLogout(); // 기존 사용자 정보 삭제 (임시) - 이 부분은 제거하는 것이 좋습니다.
            // 실제 구현에서는 새로운 사용자 정보로 AuthContext를 업데이트하는 함수를 호출해야 함
            // 예를 들어, AuthContext에 `setUser` 함수가 있다면 `setAuthUser(response.data);` 사용
        } catch (err) {
            console.error('프로필 업데이트 실패:', err.response ? err.response.data : err.message);
            showTemporaryMessage(err.response?.data?.message || '프로필 업데이트에 실패했습니다.', true);
        }
    };

    // 비밀번호 변경 핸들러
    const handleChangePassword = async (event) => {
        event.preventDefault();
        setPasswordChangeError('');
        setMessage(''); // 기존 메시지 초기화
        setError(null);

        if (!oldPassword || !newPassword || !confirmNewPassword) {
            setPasswordChangeError('모든 비밀번호 필드를 입력해주세요.');
            return;
        }
        if (newPassword !== confirmNewPassword) {
            setPasswordChangeError('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
            return;
        }
        if (newPassword.length < 8) { // 최소 길이 등 추가 유효성 검사
            setPasswordChangeError('새 비밀번호는 최소 8자 이상이어야 합니다.');
            return;
        }
        if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/.test(newPassword)) {
            setPasswordChangeError('새 비밀번호는 8~20자, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.');
            return;
        }

        setShowConfirmModal(true);
        setConfirmMessage('비밀번호를 변경하시겠습니까?');
        setConfirmAction(async () => {
            try {
                const response = await apiClient.post('/api/auth/change-password', {
                    oldPassword,
                    newPassword
                });
                showTemporaryMessage(response.data.message || '비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.', false);
                // 비밀번호 변경 성공 후 필드 초기화 및 로그아웃
                setOldPassword('');
                setNewPassword('');
                setConfirmNewPassword('');
                setShowConfirmModal(false);
                setTimeout(() => {
                    authLogout(); // AuthContext 로그아웃
                    navigate('/login'); // 로그인 페이지로 이동
                }, 2000);
            } catch (err) {
                console.error('비밀번호 변경 실패:', err.response ? err.response.data : err.message);
                setPasswordChangeError(err.response?.data?.message || '비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.');
                setShowConfirmModal(false);
            }
        });
    };

    // 회원 탈퇴 핸들러
    const handleDeleteAccount = () => {
        setMessage(''); // 기존 메시지 초기화
        setError(null);

        setShowConfirmModal(true);
        setConfirmMessage('정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
        setConfirmAction(async () => {
            try {
                await apiClient.delete('/api/user/delete'); // 회원 탈퇴 API 호출
                showTemporaryMessage('계정이 성공적으로 삭제되었습니다. 로그인 페이지로 이동합니다.', false);
                setShowConfirmModal(false);
                setTimeout(() => {
                    authLogout(); // AuthContext 로그아웃
                    navigate('/login'); // 로그인 페이지로 이동
                }, 2000);
            } catch (err) {
                console.error('계정 삭제 실패:', err.response ? err.response.data : err.message);
                showTemporaryMessage(err.response?.data?.message || '계정 삭제에 실패했습니다.', true);
                setShowConfirmModal(false);
            }
        });
    };

    // ⭐ 중요: 모든 훅 선언 후, 조건부 렌더링을 시작합니다.
    if (loading) {
        return (
            <div className="container">
                <p>사용자 정보를 불러오는 중...</p>
            </div>
        );
    }

    // user 객체가 없는 경우 (예: 인증 실패 후 navigate되기 전)
    if (!user) {
        return (
            <div className="container">
                <p>사용자 정보를 찾을 수 없거나, 인증되지 않았습니다.</p>
                <Link to="/login">로그인</Link>
            </div>
        );
    }

    return (
        <div className="container">
            <h1>마이페이지</h1>

            {message && (
                <div className={`message-area ${isSuccess ? 'success-message' : 'error-message'}`}>
                    {message}
                </div>
            )}
            {error && (
                <div className="message-area error-message">
                    {error}
                </div>
            )}

            <h2>내 프로필</h2>
            <form onSubmit={handleProfileUpdate} id="updateProfileForm">
                <p><strong>아이디:</strong> {user.username}</p>
                <p><strong>이메일:</strong> {user.email}</p>
                <div>
                    <label htmlFor="nickname">닉네임:</label>
                    <input
                        type="text"
                        id="nickname"
                        name="nickname"
                        value={nickname}
                        onChange={(e) => setNickname(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="birth">생년월일:</label>
                    <input
                        type="date"
                        id="birth"
                        name="birth"
                        value={birth}
                        onChange={(e) => setBirth(e.target.value)}
                        required
                    />
                </div>
                <div className="gender-options">
                    <label>성별:</label>
                    <input
                        type="radio"
                        id="genderMale"
                        name="gender"
                        value="MALE"
                        checked={gender === 'MALE'}
                        onChange={(e) => setGender(e.target.value)}
                    />
                    <label htmlFor="genderMale">남성</label>
                    <input
                        type="radio"
                        id="genderFemale"
                        name="gender"
                        value="FEMALE"
                        checked={gender === 'FEMALE'}
                        onChange={(e) => setGender(e.target.value)}
                    />
                    <label htmlFor="genderFemale">여성</label>
                </div>
                <button type="submit">정보 수정</button>
            </form>

            <hr />

            <h2>비밀번호 변경</h2>
            <form onSubmit={handleChangePassword} id="changePasswordForm">
                {passwordChangeError && <div className="error-message">{passwordChangeError}</div>}
                <div>
                    <label htmlFor="oldPassword">현재 비밀번호:</label>
                    <input
                        type="password"
                        id="oldPassword"
                        name="oldPassword"
                        value={oldPassword}
                        onChange={(e) => setOldPassword(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="newPassword">새 비밀번호:</label>
                    <input
                        type="password"
                        id="newPassword"
                        name="newPassword"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="confirmNewPassword">새 비밀번호 확인:</label>
                    <input
                        type="password"
                        id="confirmNewPassword"
                        name="confirmNewPassword"
                        value={confirmNewPassword}
                        onChange={(e) => setConfirmNewPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">비밀번호 변경</button>
            </form>

            <hr />

            <h2>계정 관리</h2>
            <p>더 이상 서비스를 이용하지 않으시려면 계정을 삭제할 수 있습니다.</p>
            <button onClick={handleDeleteAccount} className="delete-account-btn">
                회원 탈퇴
            </button>

            <hr />

            <p><Link to="/forgot-password">비밀번호를 잊으셨나요? (비밀번호 찾기)</Link></p>

            {/* Confirmation Modal */}
            {showConfirmModal && (
                <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-sm mx-auto relative text-center">
                        <h5 className="text-xl font-bold text-gray-800 mb-5">확인</h5>
                        <p className="text-gray-600 mb-6">{confirmMessage}</p>
                        <div className="flex justify-center gap-4">
                            <button
                                className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-5 rounded-md transition duration-150 ease-in-out"
                                onClick={() => {
                                    if (confirmAction) confirmAction();
                                    setShowConfirmModal(false); // '예' 클릭 후 모달 닫기
                                }}
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

export default MyPage;
