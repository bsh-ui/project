package com.boot.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor; // ⭐ Lombok @RequiredArgsConstructor 추가
import lombok.extern.slf4j.Slf4j; // ⭐ Lombok @Slf4j 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // ⭐ UserDetails 임포트
import org.springframework.security.core.userdetails.UserDetailsService; // ⭐ UserDetailsService 임포트
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // ⭐ WebAuthenticationDetailsSource 임포트
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List; // ⭐ List 임포트
import java.util.Optional;

@Component
@RequiredArgsConstructor // ⭐ final 필드를 인자로 받는 생성자 자동 생성
@Slf4j // ⭐ log 객체 자동 생성
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // ⭐ @Slf4j 사용으로 불필요

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService; // ⭐ UserDetailsService 추가 주입

    // ⭐⭐ JWT 필터를 적용하지 않을 'permitAll()' 경로들을 정의합니다. ⭐⭐
    // SecurityConfig의 permitAll() 경로들과 정확히 일치해야 합니다.
    private static final List<String> EXCLUDE_URL_PREFIXES = Arrays.asList(
        "/main",             // 메인 페이지
        "/custom_login",     // 로그인 페이지
        "/signup",           // 회원가입 페이지
        "/api/signup",       // 회원가입 API
        "/api/login",        // 자체 로그인 API
        "/api/refresh-token",// 토큰 재발급 API (이 필터에서 스킵하고 컨트롤러에서만 처리)
        "/css/",             // CSS 파일 (모든 하위 경로 포함)
        "/js/",              // JS 파일 (모든 하위 경로 포함)
        "/images/",          // 이미지 파일 (모든 하위 경로 포함)
        "/favicon.ico"       // 파비콘 
        
    );

    // ⭐⭐⭐ 이 메서드를 오버라이드하여 JWT 필터가 특정 경로에서는 동작하지 않도록 합니다. ⭐⭐⭐
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        log.debug("🔍 [JwtAuthenticationFilter] shouldNotFilter 체크 URI: {}", requestURI);

        if (requestURI.startsWith("/login/oauth2/code/")) {
            log.debug("🚫 [JwtAuthenticationFilter] OAuth2 콜백 URI 제외: {}", requestURI);
            return true;
        }
        for (String prefix : EXCLUDE_URL_PREFIXES) {
            if (requestURI.startsWith(prefix)) {
                log.debug("🚫 [JwtAuthenticationFilter] 제외 대상 URI 발견: {}", requestURI);
                return true; // 이 URL은 필터를 실행하지 않습니다.
            }
        }
        return false; // 이 URL은 필터를 실행합니다.
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.debug("🌐 [JwtAuthenticationFilter] doFilterInternal 실행, 요청 URI: {}", requestURI);
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("🛑 [JwtAuthenticationFilter] Preflight OPTIONS 요청 - 필터 통과");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        // Access Token 쿠키에서 토큰 추출
        String accessToken = null;
        if (request.getCookies() != null) {
            accessToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (accessToken == null) {
            log.info("ℹ️ [JwtAuthenticationFilter] Access Token 쿠키를 찾을 수 없습니다.");
        }

        try {
            // 토큰이 존재하고 유효하면 인증 처리
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // Access Token이 유효한 경우, username을 추출하여 UserDetails 로드 후 인증 정보 설정
                String username = jwtTokenProvider.getUsernameFromToken(accessToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Spring Security의 Authentication 객체 생성 및 SecurityContextHolder에 설정
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("✅ [JwtAuthenticationFilter] JWT 토큰 유효, 사용자 인증 완료: {}", username);
            }
            // else (accessToken이 없거나 유효하지 않은 경우): 인증 정보를 설정하지 않고 다음 필터로 넘깁니다.
            // permitAll() 경로들은 shouldNotFilter에서 이미 걸러지므로,
            // 여기에 도달한 요청은 인증이 필요한 경로이며, 토큰이 없으면 Spring Security가 401 Unauthorized 처리하거나
            // loginPage()로 리다이렉트 시킬 것입니다.

        } catch (ExpiredJwtException e) {
            log.warn("⚠️ [JwtAuthenticationFilter] Access Token 만료 예외 발생: {}", e.getMessage());
            // Access Token 만료 시 Refresh Token을 확인하고 재발급 시도하는 로직은
            // 클라이언트나 별도의 Refresh Token 재발급 API(/api/refresh-token)에서 처리하는 것이 일반적입니다.
            // 필터에서 강제로 리다이렉트하는 것은 CORS나 API 호출 흐름에 문제를 일으킬 수 있어 제거했습니다.
            // Spring Security가 401 Unauthorized 응답을 하거나, 로그인 페이지로 리다이렉트하도록 맡깁니다.
        } catch (Exception ex) {
            log.error("🚫 [JwtAuthenticationFilter] JWT 인증 필터 처리 중 예외 발생: {}", ex.getMessage(), ex);
            // 필요하다면 여기서 특정 응답 코드(예: 403 Forbidden)를 설정할 수 있습니다.
            // response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // return;
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // getJwtFromCookie 및 getRefreshTokenFromCookie 메서드는 변경 없습니다.
    // (기존 코드 그대로 유지)
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