package com.boot.dto; // 적절한 DTO 패키지 경로로 변경

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 댓글 생성/수정 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    private String content; // 댓글 내용
    private Long parentId; // 대댓글인 경우 부모 댓글의 ID (최상위 댓글인 경우 null)
    // ⭐ userId 필드는 제거합니다. 컨트롤러에서 @AuthenticationPrincipal을 통해 얻습니다. ⭐
    // ⭐ postId 필드는 URL PathVariable로 받으므로 여기에는 포함하지 않습니다. ⭐
}
