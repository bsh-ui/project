package com.boot.dto; // 적절한 DTO 패키지 경로로 변경

import com.boot.domain.PostLike; // PostLike 엔티티 임포트
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponseDto {
    private Long id;
    private Long postId;
    private Long userId;
    private Boolean isLike; // true면 좋아요, false면 싫어요
    private LocalDateTime createdAt;

    public LikeResponseDto(PostLike postLike) {
        this.id = postLike.getId();
        this.postId = postLike.getPost().getId();
        this.userId = postLike.getUser().getId();
        this.isLike = postLike.getIsLike();
        this.createdAt = postLike.getCreatedAt();
    }
}
