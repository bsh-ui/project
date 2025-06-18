package com.boot.security;

import java.util.Collection;
import java.util.Map; // Map 사용을 위해 import

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.boot.domain.User; // 여러분의 User 엔티티 import

public class CustomOAuth2User implements OAuth2User, UserDetails {

    private final User user; // 데이터베이스의 User 엔티티

    // ⭐ OAuth2User의 원본 속성이 필요하다면 이 Map을 저장하도록 생성자를 확장할 수 있습니다.
    // private final Map<String, Object> attributes;
    // private final String nameAttributeKey; // OAuth2User.getName()이 반환할 속성의 키 (예: "id", "sub")

    // 생성자: User 엔티티만 받아서 초기화
    public CustomOAuth2User(User user) {
        this.user = user;
        // ⭐ 만약 원본 OAuth2 attributes가 필요하다면 여기서 저장해야 합니다.
        // this.attributes = attributes;
        // this.nameAttributeKey = nameAttributeKey;
    }

    // --- OAuth2User 인터페이스 구현 ---
    @Override
    public Map<String, Object> getAttributes() {
        // 현재는 빈 Map을 반환합니다. 만약 OAuth2 Provider로부터 받은 원본 속성(예: 프로필 사진의 고해상도 URL 등)이
        // CustomOAuth2User를 통해 필요하다면, 생성자에서 Map을 받아 저장하고 여기서 반환해야 합니다.
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // User 엔티티에서 정의된 권한을 반환합니다 (예: ROLE_USER).
        // User 엔티티의 getAuthorities() 메서드가 올바르게 구현되어 있어야 합니다.
        return user.getAuthorities();
    }

    @Override
    public String getName() {
        // OAuth2User의 'name'은 일반적으로 OAuth2 Provider의 고유 식별자(예: 'sub' 또는 'id')를 의미합니다.
        // User 엔티티의 username 필드(대부분 이메일)를 반환하도록 하여 JWT subject와 일관성을 유지할 수 있습니다.
        return user.getUsername();
    }

    // --- UserDetails 인터페이스 구현 ---
    @Override
    public String getUsername() {
        // JWT의 Subject로 사용될 로그인 식별자입니다 (일반적으로 이메일 주소).
        // User 엔티티의 username 필드가 이메일을 저장하도록 설계되어 있어야 합니다.
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        // 소셜 로그인 사용자는 비밀번호가 없으므로 null 또는 빈 문자열을 반환합니다.
        // 일반 로그인 사용자의 경우 User 엔티티에 암호화된 비밀번호가 저장되어 있습니다.
        return user.getPassword(); // User 엔티티에서 비밀번호 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부를 나타냅니다. User 엔티티의 상태를 반영하도록 수정했습니다.
        return user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부를 나타냅니다. User 엔티티의 상태를 반영하도록 수정했습니다.
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 자격 증명(비밀번호) 만료 여부를 나타냅니다. User 엔티티의 상태를 반영하도록 수정했습니다.
        return user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        // 사용자 활성화 여부를 나타냅니다. User 엔티티의 상태를 반영하도록 수정했습니다.
        return user.isEnabled();
    }

    // --- Custom Getter (필요시 추가) ---
    public User getUser() {
        // 이 CustomOAuth2User 객체가 감싸고 있는 User 엔티티를 반환합니다.
        return user;
    }

    // ⭐ 추가: JWT Subject 등으로 사용될 이메일(username)을 명시적으로 가져오는 메서드
    public String getEmail() {
        return user.getEmail(); // User 엔티티의 email 필드를 사용 (username과 동일할 수 있음)
    }

    // ⭐ 추가: JWT Subject 등으로 사용될 사용자 ID를 명시적으로 가져오는 메서드
    public Long getUserId() {
        return user.getId();
    }
}