import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { checkUsernameDuplicate, checkEmailDuplicate, checkNicknameDuplicate, signupUser } from '../services/api'; // services/api에서 API 함수 임포트

function SignupPage() {
    // 폼 입력 필드 상태
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [nickname, setNickname] = useState('');
    const [birth, setBirth] = useState('');
    const [gender, setGender] = useState('N'); // 'M', 'F', 'N' (Not selected)

    // 에러 메시지 상태 (개별 필드)
    const [usernameError, setUsernameError] = useState('');
    const [emailError, setEmailError] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [confirmPasswordError, setConfirmPasswordError] = useState('');
    const [nicknameError, setNicknameError] = useState('');
    const [genderError, setGenderError] = useState('');

    // 비밀번호 보이기/숨기기 상태
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    // 전체 폼 제출 결과 메시지 (alert() 대체)
    const [submissionMessage, setSubmissionMessage] = useState('');
    const [isSuccessMessage, setIsSuccessMessage] = useState(false);
    const messageTimeoutRef = useRef(null); // 메시지 타이머 ref

    const navigate = useNavigate();

    // 메시지 표시 헬퍼 함수
    const showTemporaryMessage = (msg, isSuccess = false) => {
        if (messageTimeoutRef.current) {
            clearTimeout(messageTimeoutRef.current);
        }
        setSubmissionMessage(msg);
        setIsSuccessMessage(isSuccess);
        messageTimeoutRef.current = setTimeout(() => {
            setSubmissionMessage('');
        }, 5000); // 5초 후 메시지 사라짐
    };

    // 비밀번호 일치 여부 확인
    useEffect(() => {
        if (confirmPassword === '') {
            hideError(setConfirmPasswordError);
            return;
        }
        if (password !== confirmPassword) {
            showError(setConfirmPasswordError, '비밀번호와 비밀번호 확인이 일치하지 않습니다.');
        } else {
            hideError(setConfirmPasswordError);
        }
    }, [password, confirmPassword]);

    // 필드 유효성 검사 및 에러 설정 함수
    const validateField = (fieldName, value) => {
        let isValid = true;
        switch (fieldName) {
            case 'username':
                if (!/^[a-zA-Z0-9]{5,20}$/.test(value)) {
                    showError(setUsernameError, '아이디는 5~20자의 영문 대소문자, 숫자로만 구성되어야 합니다.');
                    isValid = false;
                } else {
                    hideError(setUsernameError);
                }
                break;
            case 'email':
                if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
                    showError(setEmailError, '유효한 이메일 주소를 입력해주세요.');
                    isValid = false;
                } else {
                    hideError(setEmailError);
                }
                break;
            case 'password':
                if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?`~]).{8,20}$/.test(value)) {
                    showError(setPasswordError, '비밀번호는 8~20자의 영문, 숫자, 특수문자를 포함해야 합니다.');
                    isValid = false;
                } else {
                    hideError(setPasswordError);
                }
                break;
            case 'nickname':
                if (!/^[a-zA-Z0-9가-힣]{2,10}$/.test(value)) {
                    showError(setNicknameError, '닉네임은 2~10자의 한글, 영문, 숫자로만 구성되어야 합니다.');
                    isValid = false;
                } else {
                    hideError(setNicknameError);
                }
                break;
            case 'gender':
                if (value === 'N') {
                    showError(setGenderError, '성별을 선택해주세요.');
                    isValid = false;
                } else {
                    hideError(setGenderError);
                }
                break;
            default:
                break;
        }
        return isValid;
    };

    // 에러 메시지 표시 헬퍼 함수
    const showError = (setter, message) => {
        setter(message);
    };

    // 에러 메시지 숨기기 헬퍼 함수
    const hideError = (setter) => {
        setter('');
    };

    // 아이디/이메일/닉네임 중복 확인 (onBlur 이벤트)
    const checkDuplicate = async (field, value, errorSetter) => {
        if (!value.trim() || !validateField(field, value)) {
            return; // 값이 비어있거나 유효성 검사 실패 시 중복 확인 건너뛰기
        }

        try {
            let response;
            if (field === 'username') {
                response = await checkUsernameDuplicate(value); // services/api 함수 사용
            } else if (field === 'email') {
                response = await checkEmailDuplicate(value); // services/api 함수 사용
            } else if (field === 'nickname') {
                response = await checkNicknameDuplicate(value); // services/api 함수 사용
            }

            if (response && response.data.exists) {
                showError(errorSetter, `이미 사용 중인 ${field === 'username' ? '아이디' : field === 'email' ? '이메일' : '닉네임'}입니다.`);
            } else {
                hideError(errorSetter);
            }
        } catch (e) {
            console.error(`${field} 중복 확인 실패:`, e);
            // showError(errorSetter, `중복 확인 중 오류 발생`); // 필요 시 오류 메시지 표시
        }
    };

    // 폼 제출 핸들러
    const handleSubmit = async (event) => {
        event.preventDefault(); // 기본 폼 제출 방지

        // 모든 에러 메시지 초기화
        setSubmissionMessage('');
        setUsernameError('');
        setEmailError('');
        setPasswordError('');
        setConfirmPasswordError('');
        setNicknameError('');
        setGenderError('');


        let isValid = true;

        // 모든 필드 유효성 검사
        isValid = validateField('username', username) && isValid;
        isValid = validateField('email', email) && isValid;
        isValid = validateField('password', password) && isValid;
        isValid = validateField('nickname', nickname) && isValid;
        isValid = validateField('gender', gender) && isValid;

        // 비밀번호 확인 일치 여부 다시 체크
        if (password !== confirmPassword) {
            showError(setConfirmPasswordError, '비밀번호와 비밀번호 확인이 일치하지 않습니다.');
            isValid = false;
        }

        if (!isValid) {
            showTemporaryMessage('입력 필드를 다시 확인해주세요.', false);
            return;
        }

        const formData = {
            username: username,
            email: email,
            password: password,
            nickname: nickname,
            birth: birth,
            gender: gender
        };

        console.log("회원가입 요청 데이터:", formData);

        try {
            // services/api의 signupUser 사용
            const response = await signupUser(formData);

            console.log("서버 응답 상태:", response.status, response.statusText);

            if (response.status === 201 || response.status === 200) { // 201 Created
                showTemporaryMessage('회원가입이 성공적으로 완료되었습니다!', true);
                navigate('/login'); // 로그인 페이지로 리다이렉션
            } else {
                const errorData = response.data;
                showTemporaryMessage(`회원가입 실패: ${errorData.message || '알 수 없는 오류'}`, false);
                console.error('회원가입 실패 응답:', response.status, errorData);
            }
        } catch (error) {
            console.error('회원가입 요청 중 네트워크 오류 발생:', error);
            if (error.response && error.response.data && error.response.data.message) {
                 showTemporaryMessage(`회원가입 실패: ${error.response.data.message}`, false);
            } else {
                 showTemporaryMessage('회원가입 중 네트워크 오류가 발생했습니다. 서버가 실행 중인지 확인해주세요.', false);
            }
        }
    };

    return (
        <div className="font-sans bg-gray-100 flex justify-center items-center min-h-screen p-4">
            {submissionMessage && (
                <div className={`fixed top-4 right-4 z-50 p-4 rounded-md shadow-lg ${isSuccessMessage ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}>
                    {submissionMessage}
                </div>
            )}

            <div className="signup-container bg-white p-8 rounded-lg shadow-lg w-full max-w-md box-border">
                <h2 className="text-center text-gray-800 mb-5 text-2xl font-semibold">회원가입</h2>

                <form onSubmit={handleSubmit}>
                    <div className="form-group mb-4">
                        <label htmlFor="username" className="block mb-1 text-gray-700 font-bold text-sm">아이디 (5~20자 영문, 숫자)</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            onBlur={handleUsernameBlur}
                            required
                            minLength="5"
                            maxLength="20"
                            className="w-full p-2 border border-gray-300 rounded-md box-border text-base focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                        {usernameError && <div className="error-message text-red-600 text-sm mt-1 block">{usernameError}</div>}
                    </div>

                    <div className="form-group mb-4">
                        <label htmlFor="email" className="block mb-1 text-gray-700 font-bold text-sm">이메일</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            onBlur={handleEmailBlur}
                            required
                            className="w-full p-2 border border-gray-300 rounded-md box-border text-base focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                        {emailError && <div className="error-message text-red-600 text-sm mt-1 block">{emailError}</div>}
                    </div>

                    <div className="form-group relative">
                        <label htmlFor="password" className="block mb-1 text-gray-700 font-bold text-sm">비밀번호 (8~20자 영문, 숫자, 특수문자 포함)</label>
                        <div className="password-container relative flex items-center w-full">
                            <input
                                type={showPassword ? 'text' : 'password'}
                                id="password"
                                name="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                onBlur={handlePasswordBlur}
                                required
                                minLength="8"
                                maxLength="20"
                                className="flex-grow w-full py-2 pl-2 pr-8 border border-gray-300 rounded-md box-border text-base focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                            <button
                                type="button"
                                className="password-toggle absolute right-2 top-1/2 -translate-y-1/2 p-1 text-gray-500 hover:text-gray-700 transition duration-150 ease-in-out cursor-pointer flex items-center justify-center w-6 h-6 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-full"
                                onClick={() => setShowPassword(!showPassword)}
                            >
                                {/* Font Awesome 아이콘 직접 삽입 대신 Lucide React 아이콘 사용 고려 */}
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-eye">
                                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>
                                    {showPassword ? <path d="M2.06 12.06L21.94 12.06"/> : null}
                                </svg>
                            </button>
                        </div>
                        {passwordError && <div className="error-message text-red-600 text-sm mt-1 block">{passwordError}</div>}
                    </div>

                    <div className="form-group relative">
                        <label htmlFor="confirmPassword" className="block mb-1 text-gray-700 font-bold text-sm">비밀번호 확인</label>
                        <div className="password-container relative flex items-center w-full">
                            <input
                                type={showConfirmPassword ? 'text' : 'password'}
                                id="confirmPassword"
                                name="confirmPassword"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                                className="flex-grow w-full py-2 pl-2 pr-8 border border-gray-300 rounded-md box-border text-base focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                            <button
                                type="button"
                                className="password-toggle absolute right-2 top-1/2 -translate-y-1/2 p-1 text-gray-500 hover:text-gray-700 transition duration-150 ease-in-out cursor-pointer flex items-center justify-center w-6 h-6 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-full"
                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                            >
                                {/* Font Awesome 아이콘 직접 삽입 대신 Lucide React 아이콘 사용 고려 */}
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-eye">
                                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>
                                    {showConfirmPassword ? <path d="M2.06 12.06L21.94 12.06"/> : null}
                                </svg>
                            </button>
                        </div>
                        {confirmPasswordError && <div className="error-message text-red-600 text-sm mt-1 block">{confirmPasswordError}</div>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="nickname" className="block mb-1 text-gray-700 font-bold text-sm">닉네임 (2~10자 한글, 영문, 숫자)</label>
                        <input
                            type="text"
                            id="nickname"
                            name="nickname"
                            value={nickname}
                            onChange={(e) => setNickname(e.target.value)}
                            onBlur={handleNicknameBlur}
                            required
                            minLength="2"
                            maxLength="10"
                            className="w-full p-2 border border-gray-300 rounded-md box-border text-base focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                        {nicknameError && <div className="error-message text-red-600 text-sm mt-1 block">{nicknameError}</div>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="birth" className="block mb-1 text-gray-700 font-bold text-sm">생년월일</label>
                        <input
                            type="date"
                            id="birth"
                            name="birth"
                            value={birth}
                            onChange={(e) => setBirth(e.target.value)}
                            className="w-full p-2 border border-gray-300 rounded-md box-border text-base focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                    </div>

                    <div className="form-group">
                        <label className="block mb-1 text-gray-700 font-bold text-sm">성별</label>
                        <div className="gender-group flex space-x-4">
                            <label className="inline-block mr-4 text-sm">
                                <input
                                    type="radio"
                                    id="genderM"
                                    name="gender"
                                    value="MALE" // 백엔드 DTO에 맞게 변경 (M -> MALE)
                                    checked={gender === 'MALE'}
                                    onChange={(e) => setGender(e.target.value)}
                                    required
                                    className="mr-1"
                                /> 남자
                            </label>
                            <label className="inline-block mr-4 text-sm">
                                <input
                                    type="radio"
                                    id="genderF"
                                    name="gender"
                                    value="FEMALE" // 백엔드 DTO에 맞게 변경 (F -> FEMALE)
                                    checked={gender === 'FEMALE'}
                                    onChange={(e) => setGender(e.target.value)}
                                    required
                                    className="mr-1"
                                /> 여자
                            </label>
                        </div>
                        {genderError && <div className="error-message text-red-600 text-sm mt-1 block">{genderError}</div>}
                    </div>

                    <div className="button-container text-center mt-5">
                        <button
                            type="submit"
                            className="bg-blue-600 hover:bg-blue-700 text-white py-3 px-5 rounded-md cursor-pointer text-lg w-full box-border transition duration-150 ease-in-out"
                        >
                            회원가입
                        </button>
                    </div>
                </form>
                <Link to="/" className="link-back block text-center mt-4 text-blue-600 hover:underline transition duration-150 ease-in-out">뒤로가기</Link>
            </div>
        </div>
    );
}

export default SignupPage;
