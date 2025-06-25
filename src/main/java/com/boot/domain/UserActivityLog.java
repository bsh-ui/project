package com.boot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
// import jakarta.persistence.Temporal; // LocalDateTime 사용 시 TemporalType.TIMESTAMP는 불필요
// import jakarta.persistence.TemporalType; // LocalDateTime 사용 시 TemporalType.TIMESTAMP는 불필요
import lombok.AccessLevel; // ⭐ AccessLevel import 추가
import lombok.Builder; // ⭐ Builder import 추가
import lombok.Data;
import lombok.NoArgsConstructor; // NoArgsConstructor는 명시적으로 두는 것이 좋습니다.

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp; // ⭐ CreationTimestamp import 추가

@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 포함 (AllArgsConstructor는 포함X)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // ⭐ 기본 생성자 접근 레벨 protected로 설정, JPA 사용 위함
@Entity
@Table(name = "user_activity_log")
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // 활동을 수행한 사용자 ID

    @Column(nullable = false, length = 50)
    private String activityType; // 활동 유형 (예: "LOGIN", "LOGOUT", "OAUTH2_LOGIN", "PASSWORD_CHANGE")

    @CreationTimestamp // ⭐ 엔티티가 저장될 때 현재 시간(DB 타임스탬프)이 자동으로 주입됩니다.
    @Column(nullable = false, updatable = false) // 생성 후 업데이트 불가
    private LocalDateTime timestamp; // 활동 발생 시간

    @Column(length = 45) // IPv4 (15자) + IPv6 (39자) 고려하여 충분한 길이
    private String ipAddress; // 활동이 발생한 IP 주소

    @Column(length = 500) // 세부 정보 길이 증가 (더 많은 내용 기록 가능)
    private String details; // 활동에 대한 추가 상세 정보 (예: "로그인 성공", "잘못된 비밀번호")

    // ⭐ Builder 패턴을 위한 생성자 ⭐
    // @Builder 어노테이션은 이 생성자를 활용하여 빌더 메서드를 제공합니다.
    @Builder
    public UserActivityLog(Long userId, String activityType, String ipAddress, String details) {
        this.userId = userId;
        this.activityType = activityType;
        this.ipAddress = ipAddress;
        this.details = details;
        // timestamp는 @CreationTimestamp에 의해 자동으로 설정되므로 여기에 포함하지 않습니다.
    }

    // @Data 어노테이션이 Getter, Setter를 자동으로 생성하므로 수동 작성된 getter/setter는 제거했습니다.
}