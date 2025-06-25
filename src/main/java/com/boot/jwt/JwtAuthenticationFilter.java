package com.boot.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // ⭐⭐⭐ JWT 필터를 적용하지 않을 'permitAll()' 경로들을 정의합니다. ⭐⭐⭐
    // SecurityConfig의 permitAll() 경로들과 정확히 일치해야 합니다.
    private static final List<String> EXCLUDE_URL_PREFIXES = Arrays.asList(
        "/", // React 앱 루트 (매우 중요!)
        "/main", // 메인 페이지
        "/login", // React 로그인 페이지
        "/signup", // React 회원가입 페이지
        "/forgot-password", // React 비밀번호 찾기
        "/custom_login", // Spring Security 폼 로그인 페이지

        // 인증 관련 public API
        // POST 요청의 경우 shouldNotFilter에서 HttpMethod로 분기 처리하는 것이 더 정확합니다.
        // 여기서는 URI 접두사만으로 일단 필터에서 제외되도록 합니다.
        "/api/auth/authenticate", // 일반 로그인 API
        "/api/auth/signup", // 회원가입 API
        "/api/check-username", // 사용자 이름 중복 확인
        "/api/check-email", // 이메일 중복 확인
        "/api/check-nickname", // 닉네임 중복 확인
        "/api/auth/refresh-token", // 토큰 재발급 API
        "/api/auth/find-username", // 사용자 이름 찾기
        "/api/auth/reset-password", // 비밀번호 재설정

        "/oauth2/authorization/", // OAuth2 로그인 시작 (예: /oauth2/authorization/google)
        "/login/oauth2/code/", // OAuth2 콜백 (예: /login/oauth2/code/google)

        // Spring Boot의 정적 자원들 (React 빌드에 포함되지 않는 이미지, 파일 등)
        // React 앱의 정적 파일들은 웹 서버나 React 개발 서버가 직접 서빙하지만,
        // 혹시 Spring Boot가 서빙하는 정적 자원이 있다면 여기에 추가합니다.
        "/static/",
        "/css/",
        "/js/",
        "/images/",
        "/uploads/", // 업로드된 파일
        "/files/cover-image/", // 커버 이미지 파일 접근
        "/favicon.ico",
        "/manifest.json",
        "/asset-manifest.json",
        "/robots.txt",
        "/logo" // logo192.png, logo512.png 등
    );

    // ⭐⭐⭐ 이 메서드를 오버라이드하여 JWT 필터가 특정 경로에서는 동작하지 않도록 합니다. ⭐⭐⭐
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod(); // HTTP 메서드도 확인

        log.debug("🔍 [JwtAuthenticationFilter] shouldNotFilter 체크 URI: {} (메서드: {})", requestURI, method);

        // OPTIONS 요청은 항상 필터링하지 않음 (CORS Preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("🚫 [JwtAuthenticationFilter] Preflight OPTIONS 요청 - 필터 제외");
            return true;
        }

        // 특정 API 요청 중 GET 메서드는 필터링하지 않음
        // (이 부분은 EXCLUDE_URL_PREFIXES에 포함되지 않는 동적 GET API 경로를 처리하기 위함)
        if ("GET".equalsIgnoreCase(method)) {
            if (requestURI.startsWith("/api/music") ||
                requestURI.startsWith("/api/posts") ||
                requestURI.startsWith("/api/playlists") ||
                requestURI.startsWith("/api/notices") ||
                requestURI.startsWith("/api/files/cover-image/")) {
                log.debug("🚫 [JwtAuthenticationFilter] 공개 GET API 요청 - 필터 제외: {}", requestURI);
                return true;
            }
        }
        
        // EXCLUDE_URL_PREFIXES 리스트에 포함된 경로 확인
        for (String prefix : EXCLUDE_URL_PREFIXES) {
            // 정확히 일치하거나 접두사로 시작하는 경우
            if (requestURI.equals(prefix) || requestURI.startsWith(prefix)) {
                log.debug("🚫 [JwtAuthenticationFilter] 제외 대상 URI 발견: {} (접두사: {})", requestURI, prefix);
                return true; // 이 URL은 필터를 실행하지 않습니다.
            }
        }
        
        log.debug("✅ [JwtAuthenticationFilter] 필터 적용 대상 URI: {}", requestURI);
        return false; // 이 URL은 필터를 실행합니다.
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.debug("🌐 [JwtAuthenticationFilter] doFilterInternal 실행, 요청 URI: {} (메서드: {})", requestURI, method);

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
                String username = jwtTokenProvider.getUsernameFromToken(accessToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("✅ [JwtAuthenticationFilter] JWT 토큰 유효, 사용자 인증 완료: {}", username);
            }
        } catch (ExpiredJwtException e) {
            log.warn("⚠️ [JwtAuthenticationFilter] Access Token 만료 예외 발생: {}", e.getMessage());
            // 만료된 토큰에 대해서는 Spring Security가 이후에 401 Unauthorized를 발생시키도록 합니다.
            // 클라이언트에서 토큰 재발급 로직을 처리해야 합니다.
        } catch (Exception ex) {
            log.error("🚫 [JwtAuthenticationFilter] JWT 인증 필터 처리 중 예외 발생: {}", ex.getMessage(), ex);
            // 모든 예외를 여기서 잡아도 다음 필터 체인이 실행되도록 합니다.
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

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
