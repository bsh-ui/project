package com.boot.jwt;

import io.jsonwebtoken.ExpiredJwtException; // JWT 만료 예외 import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // SimpleGrantedAuthority import 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // UsernamePasswordAuthenticationToken import 추가
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        logger.debug("🌐 [JwtAuthenticationFilter] 요청 URI: {}", requestUri);

        // Refresh Token 재발급 요청은 이 필터에서 토큰 검증을 스킵
        // SecurityConfig에서 이 경로는 permitAll() 처리되어 있음
        if (requestUri.equals("/api/refresh-token")) {
            logger.debug("🔄 [JwtAuthenticationFilter] Refresh Token 재발급 요청이므로 필터 스킵.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 요청에서 Access Token 쿠키 추출
            String jwt = getJwtFromCookie(request);
            
            if (jwt != null) {
                // 2. Access Token 유효성 검사
                if (jwtTokenProvider.validateToken(jwt)) {
                    // Access Token이 유효한 경우, 인증 정보를 SecurityContext에 설정
                    logger.debug("🔑 [JwtAuthenticationFilter] 유효한 Access Token 발견. 인증 정보 설정.");
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.warn("⚠️ [JwtAuthenticationFilter] Access Token이 유효하지 않거나 만료되었습니다. Refresh Token 확인.");
                    // Access Token이 유효하지 않거나 만료된 경우, Refresh Token을 확인
                    String refreshToken = getRefreshTokenFromCookie(request);
                    if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                        // Refresh Token이 유효하면, Access Token 재발급 요청으로 리다이렉트
                        logger.info("🔄 [JwtAuthenticationFilter] 유효한 Refresh Token 발견. Access Token 재발급 요청으로 리다이렉트.");
                        response.sendRedirect("/api/refresh-token");
                        return; // 중요: 더 이상 필터 체인 진행하지 않고 리다이렉트
                    } else {
                        logger.warn("❌ [JwtAuthenticationFilter] Refresh Token도 유효하지 않거나 없습니다. 다시 로그인해야 합니다.");
                        // Refresh Token도 유효하지 않으면, 인증 실패로 간주하고 다음 필터로 넘겨 Spring Security의 기본 동작 (로그인 페이지 리다이렉트 등)을 따름
                    }
                }
            } else {
                logger.debug("ℹ️ [JwtAuthenticationFilter] Access Token 쿠키를 찾을 수 없습니다.");
            }

        } catch (ExpiredJwtException e) {
            logger.warn("⚠️ [JwtAuthenticationFilter] Access Token 만료 예외 발생: {}", e.getMessage());
            // Access Token 만료 예외가 발생하면 Refresh Token을 확인하고 재발급 시도
            String refreshToken = getRefreshTokenFromCookie(request);
            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                logger.info("🔄 [JwtAuthenticationFilter] 만료된 Access Token. Refresh Token으로 재발급 시도.");
                response.sendRedirect("/api/refresh-token");
                return; // 중요: 더 이상 필터 체인 진행하지 않고 리다이렉트
            } else {
                logger.warn("❌ [JwtAuthenticationFilter] Refresh Token도 유효하지 않거나 없습니다. 다시 로그인해야 합니다.");
            }
        } catch (Exception ex) {
            logger.error("🚫 [JwtAuthenticationFilter] JWT 인증 필터 처리 중 예외 발생: {}", ex.getMessage(), ex);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // "jwt_token" 이름의 쿠키에서 JWT 값 추출
    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> jwtCookie = Arrays.stream(cookies)
                    .filter(cookie -> "jwt_token".equals(cookie.getName()))
                    .findFirst();
            return jwtCookie.map(Cookie::getValue).orElse(null);
        }
        return null;
    }

    // "refresh_token" 이름의 쿠키에서 Refresh Token 값 추출
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> refreshTokenCookie = Arrays.stream(cookies)
                    .filter(cookie -> "refresh_token".equals(cookie.getName()))
                    .findFirst();
            return refreshTokenCookie.map(Cookie::getValue).orElse(null);
        }
        return null;
    }
}