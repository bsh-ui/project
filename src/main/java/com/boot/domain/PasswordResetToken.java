package com.boot.domain;

import jakarta.persistence.*; // JPA 어노테이션 임포트
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // LocalDateTime 임포트

@Entity // JPA 엔티티임을 명시
@Table(name = "password_reset_tokens") // 데이터베이스 테이블 이름 지정
@Data // Lombok: Getter, Setter, EqualsAndHashCode, ToString 등 자동 생성
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자 자동 생성
@Builder // Lombok: 빌더 패턴 자동 생성
public class PasswordResetToken {

    @Id // 기본 키(Primary Key)임을 명시
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성 전략: 데이터베이스에 위임 (AUTO_INCREMENT)
    private Long id;

    @Column(nullable = false, unique = true) // null 허용 안 함, 유니크 제약 조건 추가
    private String token; // 재설정 토큰 문자열 (UUID로 생성)

    // User 엔티티와 1:1 관계. PasswordResetToken은 User에 속해 있음.
    // fetch = FetchType.EAGER: 토큰을 로드할 때 사용자 정보도 함께 로드 (일반적으로 Lazy 사용하지만, 여기서는 사용자 정보가 즉시 필요하므로 EAGER)
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id") // 외래 키(Foreign Key) 설정, user_id 컬럼으로 매핑
    private User user; // 이 토큰이 속한 사용자 엔티티

    @Column(nullable = false) // null 허용 안 함
    private LocalDateTime expiryDate; // 토큰 만료 시간

    /**
     * 토큰이 만료되었는지 확인하는 헬퍼 메서드
     * @return 토큰이 현재 시간보다 이후에 만료되면 true, 그렇지 않으면 false
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}