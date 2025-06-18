package com.boot.security; // 이 패키지 경로가 프로젝트의 security 패키지와 일치하는지 확인해주세요.

import com.boot.domain.User; // 사용자님의 User 엔티티 임포트
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

// CustomUserDetails는 Spring Security의 UserDetails 인터페이스를 구현합니다.
// 이 클래스는 실제 User 엔티티의 정보를 Spring Security에 맞게 변환하고 캡슐화합니다.
public class CustomUserDetails_backup implements UserDetails {

    private final User user; // 실제 User 엔티티를 필드로 가집니다.
    private Long id; // 사용자의 고유 ID
    private String username;
    private String password;
    private boolean enabled;
    private boolean accountLocked;
    private Set<? extends GrantedAuthority> authorities;

    public CustomUserDetails_backup(User user) {
        this.user = user;
        this.id = user.getId(); // ⭐ID 설정⭐
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.accountLocked = user.isAccountLocked();
        // roles는 Set<Role>이므로 GrantedAuthority로 변환 필요
        this.authorities = user.getRoles().stream()
                                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.name()))
                                .collect(Collectors.toSet());
    }

    // ⭐ User 엔티티 자체를 반환하는 getter. SecurityUtil에서 User 엔티티에 접근할 때 사용됩니다.
    public User getUser() {
        return user;
    }

    // ⭐ User 엔티티의 ID를 직접 반환하는 getter. SecurityUtil에서 ID를 가져올 때 사용하기 편리합니다.
    public Long getId() {
        return user.getId();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name())) // getName()으로 문자열 추출
                .collect(Collectors.toSet());
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // 로그인 시 사용되는 실제 사용자 이름 (아이디 또는 이메일)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true면 만료되지 않음)
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isAccountLocked(); // 계정 잠금 여부 (User 엔티티의 accountLocked 필드 사용)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명(비밀번호) 만료 여부 (true면 만료되지 않음)
    }

    @Override
    public boolean isEnabled() {
        return true; // 사용자 활성화 여부 (true면 활성화됨)
    }
}