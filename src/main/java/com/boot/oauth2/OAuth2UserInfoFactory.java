package com.boot.oauth2;

import java.util.Map;

// OAuth2UserInfo 구현체를 생성하는 팩토리 클래스
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        // 네이버 OAuth2 제공자 처리
        if ("naver".equalsIgnoreCase(registrationId)) {
            return new NaverOAuth2UserInfo(attributes);
        }
        // 구글 OAuth2 제공자 처리
        else if ("google".equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        // ⭐ 추가: 카카오(Kakao) OAuth2 제공자 처리 ⭐
        else if ("kakao".equalsIgnoreCase(registrationId)) {
            return new KakaoOAuth2UserInfo(attributes);
        }
        // 지원하지 않는 OAuth2 제공자일 경우 예외 발생
        else {
            throw new IllegalArgumentException("지원하지 않는 OAuth2 Provider입니다: " + registrationId);
        }
    }
}