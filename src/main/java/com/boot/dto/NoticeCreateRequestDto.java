// src/main/java/com/boot/dto/NoticeCreateRequestDto.java
package com.boot.dto; // 패키지 경로는 실제 프로젝트에 맞게 수정해주세요.

import com.boot.domain.NoticeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // Request DTO는 주로 Setter를 사용합니다.
public class NoticeCreateRequestDto {
    private String title;
    private String content;
    private NoticeType type; // "NOTICE" 또는 "EVENT"
}