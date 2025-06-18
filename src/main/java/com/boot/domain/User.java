package com.boot.domain;

import com.boot.dto.UserDTO; // UserDTO 임포트 유지
import jakarta.persistence.*; // JPA 관련 어노테이션 임포트
import lombok.*; // Lombok 어노테이션 임포트

import org.hibernate.annotations.CreationTimestamp; // 생성 시간 자동 주입을 위해 추가
import org.hibernate.annotations.UpdateTimestamp; // 업데이트 시간 자동 주입을 위해 추가
import org.springframework.security.core.GrantedAuthority; // Spring Security 권한 인터페이스
import org.springframework.security.core.authority.SimpleGrantedAuthority; // GrantedAuthority 구현체
import org.springframework.security.core.userdetails.UserDetails; // Spring Security UserDetails 인터페이스

import java.time.LocalDate;
import java.time.LocalDateTime; // LocalDateTime 사용을 위해 추가
import java.util.Collection; // 권한 컬렉션을 위해 사용
import java.util.Collections; // Set 초기화를 위해 사용
import java.util.HashSet; // Set 타입을 위해 사용
import java.util.Set; // Set 타입을 위해 사용
import java.util.stream.Collectors; // 스트림을 통한 변환을 위해 사용

@Getter // Lombok: 모든 필드의 Getter 메소드 자동 생성
@Setter // Lombok: 모든 필드의 Setter 메소드 자동 생성
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자 자동 생성
@Builder // Lombok: 빌더 패턴을 사용하여 객체 생성 가능
@Entity // 이 클래스가 JPA 엔티티임을 나타냄
@Table(name = "users") // 매핑될 DB 테이블 이름 지정 (테이블명이 User가 아니라 users이므로 명시)
public class User implements UserDetails { // ⭐ 변경: UserDetails 인터페이스 구현 ⭐

    @Id // 기본 키(Primary Key)임을 나타냄
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성 전략 (MySQL의 AUTO_INCREMENT와 동일)
    private Long id; // 사용자 고유 ID (PK)

    @Column(nullable = false, unique = true, length = 50) // NOT NULL, UNIQUE, 길이 50
    private String username; // 사용자 이름 (로그인 ID, 주로 이메일을 사용)

    @Column(nullable = false, unique = true, length = 100) // NOT NULL, UNIQUE, 길이 100
    private String email; // 사용자 이메일 (소셜 로그인 시에도 사용될 수 있음)

    @Column(nullable = false, length = 255) // NOT NULL, 길이 255 (비밀번호 암호화 시 길이가 길어짐)
    private String password; // 사용자 비밀번호 (암호화된 비밀번호)

    @Column(length = 50)
    private String nickname; // 사용자 닉네임

    @Column(name = "profile_picture_url", length = 500) // DB 컬럼명 및 길이 설정
    private String picture; // 프로필 사진 경로 (DB에 저장될 전체 상대 경로)

    @Column(length = 20)
    private String provider; // OAuth2 제공자 (예: "google", "naver", "kakao")

    @Column(length = 255)
    private String providerId; // OAuth2 제공자의 고유 ID (예: 구글 sub, 네이버 id)

    @Column(length = 10)
    private LocalDate birth; // 생년월일 (YYYY-MM-DD 형식)

    @Column(length = 1)
    private String gender; // 성별 (M: 남자, F: 여자)

    @CreationTimestamp // 엔티티가 생성될 때 현재 시간 자동 주입
    @Column(name = "created_at", updatable = false) // DB 컬럼명과 매핑, 생성 후 업데이트되지 않음
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티가 업데이트될 때 현재 시간 자동 주입
    @Column(name = "updated_at") // DB 컬럼명과 매핑
    private LocalDateTime updatedAt;

    // ⭐ 변경: roles 필드를 Set<Role> 타입으로 변경 ⭐
    @ElementCollection(fetch = FetchType.EAGER) // EAGER 로딩으로 즉시 로드 (권한은 항상 필요)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // 중간 테이블 설정
    @Enumerated(EnumType.STRING) // Enum 이름을 DB에 문자열로 저장 (ROLE_USER, ROLE_ADMIN)
    @Column(name = "role") // 중간 테이블의 컬럼명
    @Builder.Default // Lombok Builder 사용 시 기본값 설정
    private Set<Role> roles =  new HashSet<>(Collections.singleton(Role.ROLE_USER)); // 기본 역할: ROLE_USER

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false; // 기본값 false: 계정이 잠겨있지 않음

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0; // 기본값 0: 로그인 실패 횟수

    @Column(name = "lock_time") // nullable = true가 기본값
    private LocalDateTime lockTime; // 계정 잠금 시간 (자동 잠금 해제에 사용)

    /**
     * OAuth2 로그인 시 사용자 정보 업데이트를 위한 편의 메서드
     * 비밀번호는 여기서 업데이트하지 않음 (OAuth2 사용자는 비밀번호 로그인 방식이 다름)
     *
     * @param username 업데이트할 사용자 이름 (소셜 로그인 시 이메일)
     * @param nickname 업데이트할 닉네임
     * @param picture 업데이트할 프로필 사진 URL
     * @param birth 업데이트할 생년월일 (null 가능)
     * @param gender 업데이트할 성별 (null 가능)
     */
    public void update(String username, String nickname, String picture, LocalDate birth, String gender) {
        this.username = username; // username 필드를 이메일 또는 사용자 식별자로 업데이트
        this.nickname = nickname;
        this.picture = picture;
        if (birth != null) {
            this.birth = birth;
        }
        if (gender != null) {
            this.gender = gender;
        }
    }

    /**
     * User 엔티티를 UserDTO로 변환하는 메서드
     *
     * @return 변환된 UserDTO 객체
     */
    public UserDTO toDTO() {
        return UserDTO.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .password(this.password) // DTO로 변환 시 비밀번호도 포함 (필요에 따라 제외 가능)
                .nickname(this.nickname)
                .picture(this.picture)
                .provider(this.provider)
                .providerId(this.providerId)
                .birth(this.birth)
                .gender(this.gender)
                .createdAt(this.createdAt) // DB 스키마에 맞게 추가
                .updatedAt(this.updatedAt) // DB 스키마에 맞게 추가
                // ⭐ 변경: Set<Role>을 Set<String>으로 변환하여 DTO에 전달 ⭐
                .roles(this.roles.stream().map(Enum::name).collect(Collectors.toSet()))
                .build();
    }

    // ⭐ UserDetails 인터페이스 구현 메서드 시작 ⭐

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // User 엔티티의 Set<Role>을 Spring Security의 Collection<? extends GrantedAuthority>로 변환
        return this.roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name())) // Role enum의 이름을 GrantedAuthority로 변환 (예: "ROLE_USER")
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        // 사용자의 암호화된 비밀번호를 반환 (소셜 로그인 사용자는 null 또는 빈 문자열)
        return this.password;
    }

    @Override
    public String getUsername() {
        // Spring Security에서 사용자의 고유 식별자로 사용될 필드를 반환 (여기서는 이메일이 저장된 username 필드)
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부를 나타냅니다. (true: 만료되지 않음)
        // 실제 만료 로직이 있다면 여기에 구현
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부를 나타냅니다. (true: 잠겨있지 않음)
        return !this.accountLocked; // accountLocked가 true이면 잠긴 상태이므로 false 반환
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 자격 증명(비밀번호) 만료 여부를 나타냅니다. (true: 만료되지 않음)
        // 실제 만료 로직이 있다면 여기에 구현
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 사용자 활성화 여부를 나타냅니다. (true: 활성화됨)
        // 실제 활성화/비활성화 로직이 있다면 여기에 구현
        return true;
    }

    // ⭐ UserDetails 인터페이스 구현 메서드 끝 ⭐


    // --- 기존 편의 메서드들 ---
    public void updatePicture(String picture) {
        this.picture = picture;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lockAccount() {
        this.accountLocked = true;
        this.lockTime = LocalDateTime.now();
    }

    public void unlockAccount() {
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
        this.lockTime = null;
    }

    // ⭐ 추가: 역할을 Set<Role> 형태로 설정하는 메서드 (외부에서 주입 시)
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // ⭐ 추가: 단일 역할을 추가하는 메서드
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

	public void setEnabled(boolean b) {
		// TODO Auto-generated method stub
		
	}
}