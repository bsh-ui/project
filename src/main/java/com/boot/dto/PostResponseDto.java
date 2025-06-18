package com.boot.dto; // 적절한 DTO 패키지 경로로 변경

import com.boot.domain.Post; // Post 엔티티 경로에 맞게 수정
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// 게시글 응답 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorUsername; // 작성자의 로그인 ID (username)
    private String authorNickname; // ⭐ 작성자의 닉네임 (User 엔티티의 nickname 사용) ⭐
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likesCount;
    private Long dislikesCount;
    private Boolean isDeleted;
     private List<CommentResponseDto> comments; 
    // Post 엔티티로부터 DTO를 생성하는 생성자
    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorId = post.getAuthor().getId();
        this.authorUsername = post.getAuthor().getUsername();
        this.authorNickname = post.getAuthor().getNickname(); // User 엔티티의 nickname 필드 사용
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.isDeleted = post.getIsDeleted();
        this.likesCount = 0L; // 임시 초기화. 실제 값은 서비스에서 주입
        this.dislikesCount = 0L; // 임시 초기화. 실제 값은 서비스에서 주입
        this.comments = null;
    }
}
