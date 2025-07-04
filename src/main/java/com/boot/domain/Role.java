package com.boot.domain;

// Spring Security의 GrantedAuthority 인터페이스를 구현하는 Role Enum
public enum Role {
    // 반드시 "ROLE_" 접두사를 사용해야 합니다.
    ROLE_USER,
    ROLE_ADMIN; // 필요한 다른 역할 추가 가능
}