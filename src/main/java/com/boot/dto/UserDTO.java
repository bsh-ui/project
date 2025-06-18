package com.boot.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set; // Set 타입을 위해 임포트

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String nickname;
    private LocalDate birth;
    private String gender;

    // DB 스키마와 User 엔티티에 맞춰 created_at과 updated_at 추가
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String picture;
    private String provider;
    private String providerId;

    // roles 필드를 Set<String> 타입으로 정의 (애플리케이션 로직 편의성)
   private Set<String> roles; // (예: {"ROLE_USER", "ROLE_ADMIN"})
   
   // 계정 잠금 관련 필드 (DTO에 포함 여부는 필요에 따라 결정)
   private boolean accountLocked;
   private int failedLoginAttempts;
   private LocalDateTime lockTime;
}
