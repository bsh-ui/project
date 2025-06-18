package com.boot.domain; // User, Post 엔티티와 같은 패키지에 있거나 적절한 패키지로 변경

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes") // '좋아요' 및 '싫어요' 정보를 저장할 테이블명
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // 어떤 게시글에 대한 좋아요/싫어요인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 어떤 사용자가 좋아요/싫어요를 눌렀는지

    @Column(nullable = false)
    private Boolean isLike; // true면 좋아요, false면 싫어요

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 편의를 위한 생성자
    public PostLike(Post post, User user, Boolean isLike) {
        this.post = post;
        this.user = user;
        this.isLike = isLike;
    }
}
