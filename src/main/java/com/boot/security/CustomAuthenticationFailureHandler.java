package com.boot.security;

import com.boot.domain.User;
import com.boot.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder; // ⭐ 추가: URL 인코딩을 위해
import java.nio.charset.StandardCharsets; // ⭐ 추가: URL 인코딩을 위해
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 30;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        log.warn("⚠️ [CustomAuthenticationFailureHandler] onAuthenticationFailure 진입");
        String username = request.getParameter("username");
        final AtomicReference<String> errorMessageRef = new AtomicReference<>();

        if (username != null && !username.isEmpty()) {
            userRepository.findByUsername(username).ifPresent(user -> {
                if (user.isAccountLocked()) {
                    log.warn("🚨 계정 잠금 상태에서 로그인 실패 시도: {}", username);
                    if (user.getLockTime() != null && user.getLockTime().plusMinutes(LOCK_TIME_MINUTES).isBefore(LocalDateTime.now())) {
                        user.unlockAccount();
                        userRepository.save(user);
                        log.info("🔓 계정 잠금 해제: {} (자동 해제)", username);
                        errorMessageRef.set("계정이 잠금 해제되었습니다. 아이디 또는 비밀번호를 다시 확인해주세요.");
                    } else {
                        errorMessageRef.set("계정이 잠겨있습니다. " + LOCK_TIME_MINUTES + "분 후 다시 시도해주세요.");
                    }
                } else {
                    user.incrementFailedLoginAttempts();
                    log.warn("❌ 로그인 실패 횟수 증가: {} (현재 실패 횟수: {})", username, user.getFailedLoginAttempts());

                    if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                        user.lockAccount();
                        log.warn("🔒 계정 잠금: {} (실패 횟수 {}회 초과)", username, MAX_FAILED_ATTEMPTS);
                        errorMessageRef.set("로그인 시도 횟수 초과로 계정이 잠겼습니다. " + LOCK_TIME_MINUTES + "분 후 다시 시도해주세요.");
                    } else {
                        errorMessageRef.set("아이디 또는 비밀번호가 올바르지 않습니다. (남은 시도: " + (MAX_FAILED_ATTEMPTS - user.getFailedLoginAttempts()) + "회)");
                    }
                }
                userRepository.save(user);
            });
        }
        String finalErrorMessage = errorMessageRef.get();
        if (finalErrorMessage == null) {
            if (exception instanceof BadCredentialsException) {
                finalErrorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
            } else if (exception instanceof LockedException) {
                finalErrorMessage = "계정이 잠겨있습니다. " + LOCK_TIME_MINUTES + "분 후 다시 시도해주세요.";
            } else if (exception instanceof UsernameNotFoundException) {
                finalErrorMessage = "존재하지 않는 사용자입니다.";
            } else {
                finalErrorMessage = "알 수 없는 이유로 로그인에 실패했습니다.";
            }
        }
        
        // ⭐ 쿼리 파라미터로 메시지 전달 및 리다이렉트
        String encodedErrorMessage = URLEncoder.encode(finalErrorMessage, StandardCharsets.UTF_8.toString());
        response.sendRedirect("/custom_login?error=true&message=" + encodedErrorMessage);
    }
}