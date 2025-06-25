// src/main/java/com/boot/domain/Notice.java
package com.boot.domain; // 패키지 경로는 실제 프로젝트에 맞게 수정해주세요.

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA에서는 기본 생성자가 protected 이상이어야 합니다.
@EntityListeners(AuditingEntityListener.class) // @CreatedDate, @LastModifiedDate 사용을 위해 필요
@ToString(exclude = {"author"}) // author 객체는 순환 참조 방지를 위해 제외
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 (필요할 때만 User 정보 로딩)
    @JoinColumn(name = "author_id", nullable = true) // author_id 컬럼에 매핑, ON DELETE SET NULL 이므로 nullable
    private User author; // User Entity와 연관

    @Column(name = "view_count", nullable = false)
    private Long viewCount; // 테이블 스키마가 BIGINT이므로 Long으로 매핑

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    @Column(name = "type", nullable = false, length = 20)
    private NoticeType type; // 공지사항 타입 (NOTICE, EVENT 등)

    // 생성자 (작성 시 사용)
    @Builder
    public Notice(String title, String content, User author, NoticeType type) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.type = type;
        this.viewCount = 0L; // 초기 조회수는 0
    }

    // 수정 메서드 (업데이트 시 사용)
    public void update(String title, String content, NoticeType type) {
        this.title = title;
        this.content = content;
        this.type = type;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }
}