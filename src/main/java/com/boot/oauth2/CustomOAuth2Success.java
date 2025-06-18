package com.boot.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // ⭐ 추가
import org.springframework.security.core.context.SecurityContextHolder; // ⭐ 추가

import com.boot.domain.User; // ⭐ User 엔티티 import (UserDTO 대신 User 엔티티 직접 사용)
import com.boot.jwt.JwtTokenProvider;
import com.boot.service.UserService;
import com.boot.service.UserActivityLogService; // ⭐ UserActivityLogService로 변경 (Repository 직접 접근 대신 Service)
import com.boot.util.ActivityType; // ⭐ ActivityType enum import

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // HttpSession은 필요에 따라 유지하거나 제거

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
// import org.springframework.security.oauth2.core.user.OAuth2User; // 직접 사용 대신 CustomOAuth2User 사용
import com.boot.security.CustomOAuth2User; // ⭐ CustomOAuth2User import

import java.io.IOException;
import java.time.LocalDateTime;
// import java.util.HashMap; // 필요 없음
// import java.util.Map; // 필요 없음
// import java.util.Optional; // 필요 없음

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor; // ⭐ Lombok RequiredArgsConstructor 추가

/**
 * OAuth2 로그인 성공 시 JWT 토큰 발행 및 세션 저장, 리다이렉트 처리 핸들러
 */
@Component
@RequiredArgsConstructor // ⭐ Lombok을 사용하여 final 필드들을 주입받도록 변경 (생성자 수동 작성 불필요)
public class CustomOAuth2Success implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserActivityLogService userActivityLogService; // ⭐ UserActivityLogService 주입받도록 변경

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2Success.class);

    /*
     * ⭐ Lombok @RequiredArgsConstructor 사용으로 인해 수동 생성자 제거
     * public CustomOAuth2Success(UserService userService, JwtTokenProvider jwtTokenProvider,
     * UserActivityLogService userActivityLogService) {
     * this.userService = userService;
     * this.jwtTokenProvider = jwtTokenProvider;
     * this.userActivityLogService = userActivityLogService;
     * }
     */

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        logger.info("🔥 [CustomOAuth2Success] onAuthenticationSuccess 진입");
        logger.info("🔥 [CustomOAuth2Success] onAuthenticationSuccess 호출됨");

        // ⭐ 핵심 변경: authentication.getPrincipal()은 이미 CustomOAuth2UserService에서 반환한 CustomOAuth2User 타입입니다. ⭐
        // 따라서 별도의 정보 파싱 로직 없이 CustomOAuth2User에서 필요한 정보를 가져옵니다.
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // CustomOAuth2User 내부에 있는 User 엔티티 정보 가져오기
        User userEntity = customOAuth2User.getUser();
        logger.info("✅ OAuth2 사용자 처리 완료 (신규 등록 또는 업데이트): {}", userEntity.getNickname());

        // ⭐ Spring Security Context에 새로운 Authentication 객체 설정 ⭐
        // CustomOAuth2User는 UserDetails를 구현하므로, 이를 Principal로 하는
        // UsernamePasswordAuthenticationToken을 생성하여 SecurityContext에 설정합니다.
        // 이렇게 하면 이후 요청에서 SecurityContextHolder.getContext().getAuthentication().getPrincipal()을
        // 호출하면 CustomOAuth2User 객체를 얻을 수 있으며, 이는 UserDetails로 캐스팅 가능합니다.
        UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(
            customOAuth2User,       // Principal로 CustomOAuth2User 사용
            null,                   // 소셜 로그인 사용자는 비밀번호가 없으므로 null
            customOAuth2User.getAuthorities() // CustomOAuth2User가 제공하는 권한 (User 엔티티에서 가져옴)
        );
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
        logger.info("✅ SecurityContextHolder에 CustomOAuth2User를 Principal로 하는 Authentication 객체 설정 완료.");
        logger.info("✅ 설정된 Authentication Principal Username (Email): {}", customOAuth2User.getEmail());


        // ⭐ 사용자 활동 로그 기록 (UserActivityLogService 사용) ⭐
        try {
            userActivityLogService.saveActivity(userEntity.getId(), ActivityType.OAUTH2_LOGIN,
                                                "OAuth2 로그인 성공 (" + userEntity.getProvider() + ")",
                                                request.getRemoteAddr());
            logger.info("✅ 사용자 활동 로그 저장 완료: UserID={}, Type={}", userEntity.getId(), ActivityType.OAUTH2_LOGIN);
        } catch (Exception e) {
            logger.error("⚠️ OAuth2 사용자 활동 로그 저장 중 오류 발생: {}", e.getMessage(), e);
            // 로그 저장 실패가 로그인 성공 흐름을 방해하지 않도록 예외를 다시 던지지 않습니다.
        }

        // 세션에 사용자 정보 저장 (필요하다면 유지, JWT를 사용하면 세션 의존도를 낮출 수 있음)
        HttpSession session = request.getSession();
        // User 엔티티를 DTO로 변환하여 세션에 저장하는 것이 일반적입니다.
        // userEntity.toDTO()는 UserDTO를 반환합니다.
        session.setAttribute("user", userEntity.toDTO());
        logger.info("✅ 세션에 사용자 정보 (DTO) 저장 완료 - 세션ID: {}", session.getId());


        // ⭐ JWT 토큰 생성 시 'newAuthentication' 객체 사용 ⭐
        // generateAccessToken/RefreshToken은 이제 UserDetails를 구현하는 Principal을 기대합니다.
        String accessToken = jwtTokenProvider.generateAccessToken(newAuthentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(newAuthentication);
        logger.info("🔑 JWT Access Token 생성 완료.");
        logger.info("🔑 JWT Refresh Token 생성 완료.");

        // JWT 토큰을 쿠키에 추가 (HttpOnly, Secure 등 설정)
        // Spring Security의 쿠키 이름 관례를 따를 수 있습니다 (예: 'access_token', 'refresh_token')
        // 기존 'jwt_token'과 'refresh_token' 이름 유지
        Cookie accessTokenCookie = new Cookie("jwt_token", accessToken);
        accessTokenCookie.setHttpOnly(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000)); // 밀리초를 초로 변환
        // accessTokenCookie.setSecure(true); // HTTPS 환경에서만 사용 (배포 시 활성화)
        response.addCookie(accessTokenCookie);
        logger.info("✅ Access Token을 HttpOnly 쿠키 'jwt_token'으로 추가 완료");

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000)); // 밀리초를 초로 변환
        // refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 사용 (배포 시 활성화)
        response.addCookie(refreshTokenCookie);
        logger.info("✅ Refresh Token을 HttpOnly 쿠키 'refresh_token'으로 추가 완료");

        // 최종 리다이렉션
        response.sendRedirect("/main");
        logger.info("✅ '/main'으로 리다이렉트");
    }
}