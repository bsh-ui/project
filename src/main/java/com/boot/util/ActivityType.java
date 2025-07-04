package com.boot.util;

/**
 * 사용자 활동 유형을 정의하는 enum.
 * 웹 애플리케이션 내에서 발생할 수 있는 다양한 사용자 액션을 명확하게 분류합니다.
 */
public enum ActivityType {
    // 로그인 관련 활동
    LOGIN,          // 일반 로그인 성공
    OAUTH2_LOGIN,   // OAuth2 (소셜) 로그인 성공
    LOGOUT,         // 로그아웃

    // 계정 관련 활동
    SIGNUP,         // 회원가입
    PASSWORD_CHANGE, // 비밀번호 변경
    PROFILE_UPDATE,  // 프로필 정보 업데이트
    ACCOUNT_DEACTIVATION, // 계정 비활성화 또는 탈퇴

    // 인증/인가 관련 활동 (로그인 실패 등)
    LOGIN_FAILED,   // 로그인 실패
    ACCOUNT_LOCKED, // 계정 잠금
    PASSWORD_RESET_REQUEST, // 비밀번호 재설정 요청 (이메일 등)
    PASSWORD_RESET_SUCCESS, // 비밀번호 재설정 성공

    // 기타 중요한 사용자 액션 (예시)
    POST_CREATED,   // 게시물 생성
    POST_UPDATED,   // 게시물 수정
    POST_DELETED,   // 게시물 삭제
    COMMENT_CREATED, // 댓글 생성
    MESSAGE_SENT,   // 메시지 전송
    FILE_UPLOAD,    // 파일 업로드
    FILE_DOWNLOAD,  // 파일 다운로드
    ITEM_PURCHASED; // 상품 구매 (전자상거래 등)

    // 필요에 따라 활동 유형을 더 세분화하거나 추가할 수 있습니다.
    // 각 enum 값은 고유한 의미를 가져야 합니다.
}