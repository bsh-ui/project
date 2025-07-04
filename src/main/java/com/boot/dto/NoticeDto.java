// src/main/java/com/boot/dto/NoticeDto.java
package com.boot.dto; // 패키지 경로는 실제 프로젝트에 맞게 수정해주세요.

import com.boot.domain.Notice;
import com.boot.domain.NoticeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeDto {
    private Long id;
    private String title;
    private String content;
    private String authorUsername; // 작성자 유저네임 (or 닉네임)
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private NoticeType type;

    @Builder
    public NoticeDto(Long id, String title, String content, String authorUsername, Long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt, NoticeType type) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorUsername = authorUsername;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.type = type;
    }

    // Entity -> DTO 변환 메서드 (조회 시 사용)
    public static NoticeDto fromEntity(Notice notice) {
        return NoticeDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .authorUsername(notice.getAuthor() != null ? notice.getAuthor().getUsername() : "탈퇴한 사용자") // 작성자 정보가 없을 경우 처리
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .type(notice.getType())
                .build();
    }
}