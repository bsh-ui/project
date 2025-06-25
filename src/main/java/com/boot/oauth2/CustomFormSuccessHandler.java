package com.boot.oauth2;

import com.boot.dto.UserDTO;
import com.boot.jwt.JwtTokenProvider;
import com.boot.service.UserActivityLogService;
import com.boot.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomFormSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomFormSuccessHandler.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserActivityLogService userActivityLogService;
    private final ObjectMapper objectMapper;

    // ⭐ 중요: RequestCache를 사용하여 Spring Security의 리다이렉션 동작을 제어
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomFormSuccessHandler(JwtTokenProvider jwtTokenProvider, UserService userService,
                                    UserActivityLogService userActivityLogService, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.userActivityLogService = userActivityLogService;
        this.objectMapper = objectMapper;
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

        Authentication securityContextAuth = SecurityContextHolder.getContext().getAuthentication();
        if (securityContextAuth != null) {
            logger.info("ℹ️ [CustomFormSuccessHandler] SecurityContextHolder의 사용자: {}", securityContextAuth.getName());
            logger.info("ℹ️ [CustomFormSuccessHandler] SecurityContextHolder의 권한: {}",
                    securityContextAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
        } else {
            logger.warn("⚠️ [CustomFormSuccessHandler] SecurityContextHolder에 Authentication 객체가 없습니다!");
        }

        UserDTO loggedInUserDTO = null;
        try {
            loggedInUserDTO = userService.findUserByUsernameOrEmail(usernameOrEmail);

            if (loggedInUserDTO == null) {
                logger.error("⚠️ 데이터베이스에서 일반 로그인 사용자 정보를 찾을 수 없습니다: {}", usernameOrEmail);
                sendJsonResponse(response, false, "사용자 정보를 찾을 수 없습니다.");
                return;
            }
            logger.info("✅ 일반 로그인 사용자 정보 조회 완료: {}", loggedInUserDTO.getUsername());

            userService.handleSuccessfulLogin(loggedInUserDTO.getUsername());
            logger.info("✅ 계정 잠금 상태 및 실패 횟수 초기화 완료: {}", loggedInUserDTO.getUsername());

        } catch (Exception e) {
            logger.error("⚠️ 일반 로그인 사용자 정보 조회 또는 계정 상태 업데이트 중 오류 발생: {}", e.getMessage(), e);
            sendJsonResponse(response, false, "로그인 처리 중 오류가 발생했습니다.");
            return;
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        logger.info("🔑 Access Token 생성 완료: {}", accessToken);

        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        logger.info("🔑 Refresh Token 생성 완료: {}", refreshToken);

        // JWT 토큰을 HttpOnly 쿠키에 담아 응답 (보안상 HttpOnly = true)
        Cookie accessTokenCookie = new Cookie("jwt_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(request.isSecure());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000));
        response.addCookie(accessTokenCookie);
        logger.info("✅ Access Token을 HttpOnly 쿠키 'jwt_token'으로 추가 완료");

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(request.isSecure());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
        response.addCookie(refreshTokenCookie);
        logger.info("✅ Refresh Token을 HttpOnly 쿠키 'refresh_token'으로 추가 완료");

        // 사용자 활동 로그 기록 (로그인 성공) - 서비스 계층 사용
        try {
            Long userId = loggedInUserDTO.getId();
            String ipAddress = request.getRemoteAddr();
            userActivityLogService.logLoginSuccess(userId, loggedInUserDTO.getUsername(), ipAddress);
            logger.info("✅ 사용자 활동 로그 저장 완료: UserID={}, Type={}", userId, "LOGIN");
        } catch (Exception e) {
            logger.error("⚠️ 사용자 활동 로그 저장 중 오류 발생: {}", e.getMessage(), e);
        }

        // ⭐⭐⭐ 중요: RequestCache에서 저장된 요청을 제거하여 Spring Security의 리다이렉션을 방지 ⭐⭐⭐
        // AJAX 로그인 시에는 이전에 저장된 요청으로 리다이렉트되지 않도록 requestCache를 클리어해야 합니다.
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            requestCache.removeRequest(request, response);
            logger.info("✅ RequestCache에서 저장된 요청 제거 완료 (AJAX 로그인 리다이렉션 방지)");
        }

        // JSON 응답으로 프론트엔드에 로그인 성공 정보 전달
        sendJsonResponse(response, true, "로그인에 성공했습니다.", loggedInUserDTO);
        logger.info("✅ JSON 응답으로 로그인 성공 정보 전달 완료");
        
        // 중요: 이 시점에서 응답이 완전히 커밋되어야 함. 추가적인 리다이렉션 코드 (예: response.sendRedirect)는 절대 없어야 합니다.
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        sendJsonResponse(response, success, message, null);
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, UserDTO userDTO) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        LoginSuccessResponse jsonResponseObj;
        if (userDTO != null) {
            jsonResponseObj = new LoginSuccessResponse(
                    success,
                    message,
                    userDTO.getUsername(),
                    userDTO.getNickname(),
                    userDTO.getRoles() != null ? userDTO.getRoles().stream().collect(Collectors.toList()) : List.of(),
                    userDTO.getPicture()
            );
        } else {
            jsonResponseObj = new LoginSuccessResponse(success, message, null, null, List.of(), null);
        }

        try (PrintWriter writer = response.getWriter()) {
            objectMapper.writeValue(writer, jsonResponseObj);
            // 중요: 응답 스트림을 플러시하고 닫아서 응답을 즉시 커밋합니다.
            writer.flush();
            writer.close();
        } catch (Exception e) {
            logger.error("JSON 응답 생성 및 전송 중 오류 발생", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false, \"message\":\"서버 내부 오류 발생\"}");
            response.getWriter().flush();
            response.getWriter().close();
        }
    }

    private static class LoginSuccessResponse {
        public boolean success;
        public String message;
        public String username;
        public String nickname;
        public List<String> roles;
        public String profilePictureUrl;

        public LoginSuccessResponse(boolean success, String message, String username, String nickname, List<String> roles, String profilePictureUrl) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.nickname = nickname;
            this.roles = roles;
            this.profilePictureUrl = profilePictureUrl;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getUsername() { return username; }
        public String getNickname() { return nickname; }
        public List<String> getRoles() { return roles; }
        public String getProfilePictureUrl() { return profilePictureUrl; }
    }
}
