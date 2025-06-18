package com.boot.dto; // 적절한 DTO 패키지 경로로 변경

import com.boot.domain.Comment; // Comment 엔티티 경로에 맞게 수정
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// 댓글 응답 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private Long postId;
    private Long authorId;
    private String authorUsername; // 작성자의 로그인 ID (username)
    private String authorNickname; // 작성자의 닉네임
    private Long parentId; // 부모 댓글의 ID (최상위 댓글인 경우 null)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private List<CommentResponseDto> children; // 자식 댓글 목록 (대댓글)

    // Comment 엔티티로부터 DTO를 생성하는 생성자
    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.postId = comment.getPost().getId();
        this.authorId = comment.getAuthor().getId();
        this.authorUsername = comment.getAuthor().getUsername();
        this.authorNickname = comment.getAuthor().getNickname();
        this.parentId = (comment.getParent() != null) ? comment.getParent().getId() : null;
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.isDeleted = comment.getIsDeleted();
        // 자식 댓글들이 있을 경우 재귀적으로 DTO로 변환하여 할당 (CommentService에서 처리)
        // 여기서는 필드만 정의하고, 실제 채우는 로직은 Service 계층에서 구현
        this.children = comment.getChildren() != null && !comment.getChildren().isEmpty()
                ? comment.getChildren().stream()
                        .filter(c -> !c.getIsDeleted()) // 삭제되지 않은 자식 댓글만 포함
                        .map(CommentResponseDto::new)
                        .collect(Collectors.toList())
                : null; // 자식 댓글이 없으면 null
    }
}
