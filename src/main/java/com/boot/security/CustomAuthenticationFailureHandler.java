package com.boot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 일반 폼 로그인 실패 시 처리 핸들러 (AJAX 요청에 대해서는 JSON 응답)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper; // ObjectMapper 주입

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.warn("⚠️ [CustomAuthenticationFailureHandler] onAuthenticationFailure 진입");

        String errorMessage;
        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        } else if (exception instanceof InternalAuthenticationServiceException) {
            errorMessage = "내부 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 사용자입니다.";
        } else if (exception instanceof LockedException) {
            errorMessage = "계정이 잠금되었습니다. 잠시 후 다시 시도하거나 관리자에게 문의하세요.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "비활성화된 계정입니다.";
        } else if (exception instanceof AccountExpiredException) {
            errorMessage = "만료된 계정입니다.";
        } else if (exception instanceof CredentialsExpiredException) {
            errorMessage = "비밀번호가 만료되었습니다.";
        } else {
            errorMessage = "알 수 없는 오류로 로그인에 실패했습니다.";
        }

        log.error("🚫 [CustomAuthenticationFailureHandler] 로그인 실패: {}", errorMessage, exception);

        // ⭐⭐⭐ AJAX 요청인지 확인하여 JSON 응답 또는 리다이렉션을 결정 ⭐⭐⭐
        // 'X-Requested-With' 헤더는 일반적으로 jQuery, Axios 등의 AJAX 요청에서 자동으로 추가됩니다.
        // 또는 Accept 헤더에 "application/json"이 포함되어 있는지 확인할 수도 있습니다.
        boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
                                 request.getHeader("Accept").contains("application/json");

        if (isAjaxRequest) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", errorMessage);
            errorResponse.put("timestamp", System.currentTimeMillis());

            try (PrintWriter writer = response.getWriter()) {
                objectMapper.writeValue(writer, errorResponse);
                writer.flush();
            } catch (IOException e) {
                log.error("JSON 응답 작성 중 오류 발생: {}", e.getMessage(), e);
            }
        } else {
            // 일반 웹 폼 제출 실패 시에는 기존처럼 리다이렉션
            // Spring Security의 기본 동작을 따르거나, 특정 페이지로 리다이렉션.
            // 여기서는 /custom_login 페이지로 리다이렉션하며 에러 메시지를 전달합니다.
            String redirectUrl = "/custom_login?error=true&message=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");
            response.sendRedirect(redirectUrl);
            log.info("ℹ️ [CustomAuthenticationFailureHandler] 일반 웹 폼 로그인 실패 - 리다이렉트: {}", redirectUrl);
        }
    }
}
