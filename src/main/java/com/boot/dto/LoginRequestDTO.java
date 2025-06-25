// src/main/java/com/boot/dto/LoginRequestDTO.java
package com.boot.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data // Lombok 어노테이션: getter, setter, toString, equals, hashCode 자동 생성
public class LoginRequestDTO {
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;
}