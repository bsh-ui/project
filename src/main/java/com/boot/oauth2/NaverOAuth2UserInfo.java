package com.boot.oauth2;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes; // 네이버 API 응답의 최상위 attributes
    private Map<String, Object> response;   // "response" 키 아래의 실제 사용자 정보

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        // 네이버는 실제 사용자 정보가 "response"라는 중첩된 Map에 들어있습니다.
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes; // 최상위 attributes 반환
    }

    @Override
    public String getProviderId() {
        // "response" Map에서 "id" 필드 가져오기
        return (String) response.get("id");
    }

    @Override
    public String getEmail() {
        // "response" Map에서 "email" 필드 가져오기
        return (String) response.get("email");
    }

    @Override
    public String getNickname() {
        // "response" Map에서 "nickname" 필드 가져오기
        return (String) response.get("nickname");
    }

    @Override
    public String getImageUrl() {
        // "response" Map에서 "profile_image" 필드 가져오기
        return (String) response.get("profile_image");
    }
}