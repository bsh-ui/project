// src/main/java/com/boot/dto/JwtResponseDTO.java
package com.boot.dto;

import lombok.AllArgsConstructor; // Lombok 어노테이션: 모든 필드를 포함하는 생성자 자동 생성
import lombok.Data; // Lombok 어노테이션: getter, setter, toString, equals, hashCode 자동 생성
import lombok.NoArgsConstructor; // Lombok 어노테이션: 기본 생성자 자동 생성

@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자
public class JwtResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer"; // 토큰 타입 (일반적으로 "Bearer")
    private String username; // 로그인한 사용자의 이름 또는 이메일 (클라이언트에 반환할 정보)
}