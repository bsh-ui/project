package com.boot.oauth2;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        // 구글의 고유 ID는 "sub" 필드에 있습니다.
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        // "email" 필드에서 이메일 가져오기
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        // "name" 필드에서 닉네임 또는 이름 가져오기
        return (String) attributes.get("name");
    }

    @Override
    public String getImageUrl() {
        // "picture" 필드에서 프로필 사진 URL 가져오기
        return (String) attributes.get("picture");
    }
}