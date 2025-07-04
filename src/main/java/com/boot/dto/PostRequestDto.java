package com.boot.dto; // 적절한 DTO 패키지 경로로 변경

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 게시글 생성/수정 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {
    private String title;
    private String content;
    // ⭐ authorId 필드는 제거합니다. 컨트롤러에서 @AuthenticationPrincipal을 통해 얻습니다. ⭐
}
