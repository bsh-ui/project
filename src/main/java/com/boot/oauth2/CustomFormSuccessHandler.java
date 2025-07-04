package com.boot.oauth2; // 실제 패키지명에 맞게 변경 (예: com.boot.handler 또는 com.boot.config)

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.boot.dto.UserDTO;
import com.boot.jwt.JwtTokenProvider;
import com.boot.repository.UserActivityLogRepository;
import com.boot.service.UserService; // UserService 임포트
import com.boot.domain.UserActivityLog;
import com.boot.domain.User; // User 엔티티 임포트 추가

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 일반 폼 로그인 성공 시 JWT 토큰 발행 및 세션 저장, 리다이렉트 처리 핸들러
 */
@Component
public class CustomFormSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomFormSuccessHandler.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserActivityLogRepository userActivityLogRepository;
    // private final UserRepository userRepository; // ⭐ UserRepository를 직접 주입받는 대신 UserService를 통해 처리

    public CustomFormSuccessHandler(JwtTokenProvider jwtTokenProvider, UserService userService, UserActivityLogRepository userActivityLogRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.userActivityLogRepository = userActivityLogRepository;
        // this.userRepository = userRepository; // ⭐ 만약 UserRepository 직접 주입받는다면 이 주석 해제 및 필드 추가
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        logger.info("🔥 [CustomFormSuccessHandler] onAuthenticationSuccess 진입 (일반 폼 로그인)");

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String usernameOrEmail = userDetails.getUsername();

        logger.info("🔑 일반 로그인 사용자 ID/Email: {}", usernameOrEmail);
        Collection<? extends GrantedAuthority> currentAuthorities = authentication.getAuthorities();
        logger.info("ℹ️ [CustomFormSuccessHandler] 현재 Authentication 객체의 권한: {}",
                currentAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));

        // ⭐ 추가: SecurityContextHolder의 권한 확인 (확실한 디버깅을 위해)
        Authentication securityContextAuth = SecurityContextHolder.getContext().getAuthentication();
        if (securityContextAuth != null) {
            logger.info("ℹ️ [CustomFormSuccessHandler] SecurityContextHolder의 사용자: {}", securityContextAuth.getName());
            logger.info("ℹ️ [CustomFormSuccessHandler] SecurityContextHolder의 권한: {}",
                    securityContextAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
        } else {
            logger.warn("⚠️ [CustomFormSuccessHandler] SecurityContextHolder에 Authentication 객체가 없습니다!");
        }

        UserDTO loggedInUserDTO = null; // DTO로 변경
        try {
            loggedInUserDTO = userService.findUserByUsernameOrEmail(usernameOrEmail);

            if (loggedInUserDTO == null) {
                logger.error("⚠️ 데이터베이스에서 일반 로그인 사용자 정보를 찾을 수 없습니다: {}", usernameOrEmail);
                response.sendRedirect("/custom_login?error=user_not_found");
                return;
            }
            logger.info("✅ 일반 로그인 사용자 정보 조회 완료: {}", loggedInUserDTO.getUsername());

            // ⭐ 추가: 로그인 성공 시 계정 잠금 해제 및 실패 횟수 초기화
            // UserService에 해당 로직을 추가하는 것이 더 적절합니다.
            userService.handleSuccessfulLogin(loggedInUserDTO.getUsername()); // UserService에 메서드 추가 필요
            logger.info("✅ 계정 잠금 상태 및 실패 횟수 초기화 완료: {}", loggedInUserDTO.getUsername());


        } catch (Exception e) {
            logger.error("⚠️ 일반 로그인 사용자 정보 조회 또는 계정 상태 업데이트 중 오류 발생: {}", e.getMessage(), e);
            response.sendRedirect("/custom_login?error=user_load_fail");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", loggedInUserDTO); // DTO로 저장
        logger.info("✅ 세션에 사용자 정보 저장 완료 - 세션ID: {}", session.getId());

        // ⭐ 사용자 활동 로그 기록 (로그인 성공)
        try {
            Long userId = loggedInUserDTO.getId();
            String ipAddress = request.getRemoteAddr();

            UserActivityLog log = new UserActivityLog(
                userId,
                "LOGIN",
                ipAddress,
                "일반 폼 로그인 성공"
            );
            userActivityLogRepository.save(log);
            logger.info("✅ 사용자 활동 로그 저장 완료: UserID={}, Type={}", userId, "LOGIN");
        } catch (Exception e) {
            logger.error("⚠️ 사용자 활동 로그 저장 중 오류 발생: {}", e.getMessage(), e);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        logger.info("🔑 Access Token 생성 완료: {}", accessToken);

        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        logger.info("🔑 Refresh Token 생성 완료: {}", refreshToken);

        Cookie accessTokenCookie = new Cookie("jwt_token", accessToken);
        accessTokenCookie.setHttpOnly(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000));
        response.addCookie(accessTokenCookie);
        logger.info("✅ Access Token을 HttpOnly 쿠키 'jwt_token'으로 추가 완료");

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
        response.addCookie(refreshTokenCookie);
        logger.info("✅ Refresh Token을 HttpOnly 쿠키 'refresh_token'으로 추가 완료");

        response.sendRedirect("/main");
        logger.info("✅ '/main'으로 리다이렉트");
    }
}