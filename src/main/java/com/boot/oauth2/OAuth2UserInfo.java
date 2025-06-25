package com.boot.oauth2;

import java.util.Map;

// OAuth2 제공자별 사용자 정보(Attributes)를 추상화하는 인터페이스
public interface OAuth2UserInfo {
    Map<String, Object> getAttributes(); // OAuth2 Provider에서 받은 원본 속성
    String getProviderId();             // 각 Provider의 고유 ID (예: Naver 'id', Google 'sub')
    String getEmail();                  // 사용자 이메일
    String getNickname();               // 사용자 닉네임 (또는 이름)
    String getImageUrl();               // 프로필 사진 URL
}