package com.boot.repository;


import com.boot.domain.PasswordResetToken;
import com.boot.domain.User; // ⭐ User 엔티티 임포트 경로 확인 및 수정
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 임포트

import java.util.Optional; // Optional 임포트

// JpaRepository를 상속받아 기본적인 CRUD 기능을 제공받습니다.
// <엔티티 타입, 기본 키 타입>
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    // 토큰 문자열로 PasswordResetToken 엔티티를 찾는 메서드
    Optional<PasswordResetToken> findByToken(String token);
    // User 엔티티로 PasswordResetToken 엔티티를 찾는 메서드
    Optional<PasswordResetToken> findByUser(User user);
}