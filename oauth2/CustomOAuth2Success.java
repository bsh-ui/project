package com.boot.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.boot.dto.TeamDTO;
import com.boot.jwt.JwtTokenProvider;
import com.boot.service.TeamService;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie; // Cookie 클래스 import
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 로그인 성공 시 JWT 토큰 발행 및 세션 저장, 리다이렉트 처리 핸들러
 */
@Component
public class CustomOAuth2Success implements AuthenticationSuccessHandler {

    // TeamService와 JwtTokenProvider는 필드 주입을 유지합니다.
    // Spring이 @Autowired를 통해 자동으로 주입해 줄 것입니다.
    @Autowired
    TeamService teamService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 필드 주입 유지
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2Success.class);

    // 참고: 생성자 주입을 선호한다면 아래처럼 변경 가능 (현재 필드 주입 유지를 요청하셨기에 변경하지 않음)
    // public CustomOAuth2Success(TeamService teamService, JwtTokenProvider jwtTokenProvider) {
    //     this.teamService = teamService;
    //     this.jwtTokenProvider = jwtTokenProvider;
    // }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        logger.info("🔥 [CustomOAuth2Success] onAuthenticationSuccess 진입");
        System.out.println("🔥 [CustomOAuth2Success] onAuthenticationSuccess 호출됨"); // 개발 중 디버깅용

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = null;
        if (authentication instanceof OAuth2AuthenticationToken) {
            registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            logger.info("✅ 로그인 플랫폼: {}", registrationId);
        }

        String email = null;
        String name = "사용자";
        // String oauthId = null; // 현재 사용되지 않으므로 주석 처리
        // String nickname = null; // 현재 사용되지 않으므로 주석 처리

        try {
            switch (registrationId) {
                case "naver":
                    Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
                    email = (String) responseMap.get("email");
                    name = (String) responseMap.get("name");
                    logger.info("🌐 Naver 로그인 정보: email={}, name={}", email, name);
                    break;
                case "kakao":
                    logger.debug("🧩 카카오 attributes 전체: {}", attributes);
                    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                    Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
                    email = (String) kakaoAccount.get("email");
                    name = (String) kakaoProfile.get("nickname");

                    logger.info("📡 카카오 로그인 정보 수신");
                    logger.info("📧 이메일: {}", email);
                    logger.info("👤 이름(닉네임): {}", name);
                    break;
                case "google":
                default: // google 및 기타 (default 처리)
                    email = (String) attributes.get("email");
                    name = (String) attributes.get("name");
                    logger.info("🌐 Google/기타 로그인 정보: email={}, name={}", email, name);
                    break;
            }
        } catch (Exception e) {
            logger.error("⚠️ 이메일 추출 중 오류 발생: {}", e.getMessage(), e);
            response.sendRedirect("/login?error=email_extract_fail"); // 에러 페이지 리다이렉트
            return;
        }

        logger.info("✅ 최종 추출된 이메일: {}", email);

        // DB에서 해당 이메일로 유저 조회
        TeamDTO user = teamService.find_list(email);

        if (user == null) {
            logger.warn("❌ DB에서 사용자 정보를 찾을 수 없습니다: {}. 신규 사용자 등록을 진행합니다.", email);

            HashMap<String, String> param = new HashMap<>();
            param.put("mf_id", email);
            param.put("mf_email", email);
            param.put("mf_pw", "oauth2"); // OAuth2 사용자는 고정 비밀번호 또는 임시 비밀번호
            param.put("mf_pw_chk", "oauth2");
            param.put("mf_name", name != null ? name : "사용자");
            param.put("mf_nickname", name != null ? name : "사용자");
            param.put("mf_birth", "2000-01-01"); // 기본값
            param.put("mf_gender", "m"); // 기본값

            try {
                teamService.recruit(param);
                user = teamService.find_list(email); // 등록 후 재조회
                logger.info("✅ 신규 OAuth2 사용자 등록 및 재조회 완료: {}", email);
            } catch (Exception e) {
                logger.error("⚠️ 신규 사용자 등록 중 오류 발생: {}", e.getMessage(), e);
                response.sendRedirect("/login?error=user_reg_fail");
                return;
            }
        }

        // 세션에 사용자 정보 저장 (Spring Security Context에 저장되는 것과 별개로 UI/세션 관리용)
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        logger.info("✅ 세션에 사용자 정보 저장 완료 - 세션ID: {}", session.getId());

        if (user != null) {
            // 1. Access Token 생성 (새로 추가된 메서드 사용)
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            logger.info("🔑 Access Token 생성 완료: {}", accessToken);

            // 2. Refresh Token 생성 (새로 추가된 메서드 사용)
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            logger.info("🔑 Refresh Token 생성 완료: {}", refreshToken);

            // 3. Access Token을 HttpOnly 쿠키에 추가
            // 쿠키 이름: jwt_token
            // 만료 시간: jwtTokenProvider.getAccessTokenExpiration() (밀리초) / 1000 (초)
            Cookie accessTokenCookie = new Cookie("jwt_token", accessToken);
            accessTokenCookie.setHttpOnly(false); // JavaScript 접근 불가 (보안 강화)
            accessTokenCookie.setPath("/");      // 모든 경로에서 쿠키 유효
            // accessTokenCookie.setSecure(request.isSecure()); // HTTPS 환경에서 주석 해제 권장
            accessTokenCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000));
            response.addCookie(accessTokenCookie);
            logger.info("✅ Access Token을 HttpOnly 쿠키 'jwt_token'으로 추가 완료");

            // 4. Refresh Token을 HttpOnly 쿠키에 추가
            // 쿠키 이름: refresh_token
            // 만료 시간: jwtTokenProvider.getRefreshTokenExpiration() (밀리초) / 1000 (초)
            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true); // JavaScript 접근 불가 (보안 강화)
            refreshTokenCookie.setPath("/");      // 모든 경로에서 쿠키 유효
            // refreshTokenCookie.setSecure(request.isSecure()); // HTTPS 환경에서 주석 해제 권장
            refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
            response.addCookie(refreshTokenCookie);
            logger.info("✅ Refresh Token을 HttpOnly 쿠키 'refresh_token'으로 추가 완료");

        } else {
            logger.error("⚠️ 사용자 정보 조회/등록 실패로 JWT 토큰을 발행할 수 없습니다.");
            response.sendRedirect("/login?error=token_issue_failed");
            return;
        }
        
        // 최종 리다이렉션 (로그인 성공 후 메인 페이지로 이동)
        response.sendRedirect("/main");
    }
}