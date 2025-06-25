package com.boot.domain; // User 엔티티와 같은 패키지에 있거나 적절한 패키지로 변경

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // TEXT 타입 매핑
    private String content;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩: 게시글 조회 시 항상 작성자 정보까지 한 번에 가져올 필요는 없음
    @JoinColumn(name = "author_id", nullable = false) // author_id 컬럼과 매핑
    private User author; // ⭐ 작성자 User 엔티티 참조 ⭐

    @ColumnDefault("0") // SQL DEFAULT 0과 일치
    private Long viewCount;

    @CreationTimestamp // 엔티티 생성 시 자동으로 시간 기록
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티 업데이트 시 자동으로 시간 갱신
    private LocalDateTime updatedAt;

    @ColumnDefault("false") // SQL DEFAULT FALSE와 일치
    private Boolean isDeleted; // 소프트 삭제 플래그

    // 생성자 (필수 필드만)
    public Post(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.viewCount = 0L; // 기본값 설정
        this.isDeleted = false; // 기본값 설정
    }
}
