package com.boot.oauth2;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;
    private Map<String, Object> kakaoAccount; // "kakao_account" 키 아래의 정보
    private Map<String, Object> profile;      // "kakao_account" -> "profile" 키 아래의 정보

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (this.kakaoAccount != null) {
            this.profile = (Map<String, Object>) kakaoAccount.get("profile");
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        // 카카오의 고유 ID는 최상위 "id" 필드에 있습니다.
        return String.valueOf(attributes.get("id")); // id는 Long 타입이므로 String으로 변환
    }

    @Override
    public String getEmail() {
        // "kakao_account" 아래 "email" 필드에서 이메일 가져오기
        // email_needs_agreement가 false이거나 true이고 동의한 경우에만 email 필드가 존재
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
            return (String) kakaoAccount.get("email");
        }
        return null;
    }

    @Override
    public String getNickname() {
        // "kakao_account" -> "profile" 아래 "nickname" 필드에서 닉네임 가져오기
        if (profile != null && profile.containsKey("nickname")) {
            return (String) profile.get("nickname");
        }
        return null;
    }

    @Override
    public String getImageUrl() {
        // "kakao_account" -> "profile" 아래 "profile_image_url" 필드에서 프로필 사진 URL 가져오기
        if (profile != null && profile.containsKey("profile_image_url")) {
            return (String) profile.get("profile_image_url");
        }
        return null;
    }
}