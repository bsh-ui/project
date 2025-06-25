package com.boot.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws; // Jws는 직접 사용되지 않으므로 제거 가능
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Spring Security의 UserDetails 사용
import org.springframework.stereotype.Component;

// import com.boot.security.CustomUserDetails; // CustomUserDetails는 UserDetails 인터페이스를 구현하므로 직접 사용 불필요
import com.boot.domain.User; // ⭐ User 엔티티 import: JWT Subject로 사용할 User ID/Email 가져오기 위함
import com.boot.security.CustomOAuth2User; // ⭐ CustomOAuth2User import: OAuth2 로그인 Principal 처리 위함

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret-key}")
    private String secretKeyString;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 밀리초

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 밀리초

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    /**
     * Access Token을 생성합니다.
     * 토큰에는 사용자 ID(이메일)와 권한 정보가 포함됩니다.
     * Subject는 UserDetails의 getUsername()을 사용합니다.
     *
     * @param authentication 현재 인증된 사용자 정보 (UserDetails를 구현하는 Principal)
     * @return 생성된 Access Token 문자열
     */
    public String generateAccessToken(Authentication authentication) {
        // authentication.getPrincipal()은 CustomUserDetails 또는 CustomOAuth2User가 될 수 있으며,
        // 이들은 모두 UserDetails 인터페이스를 구현합니다.
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        // ⭐ Subject로 사용할 사용자 ID 또는 이메일 결정 ⭐
        // UserDetails.getUsername()은 User 엔티티의 username 필드(이메일)를 반환하도록 되어 있습니다.
        // 이를 JWT Subject로 사용하는 것이 일반적입니다.
        String subject = principal.getUsername();
        logger.info("[JwtTokenProvider] Access Token Subject 설정 (Username from UserDetails): {}", subject);

        // 사용자의 권한들을 쉼표로 구분된 문자열로 변환하여 클레임에 추가
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date expiryDate = new Date(now + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(subject) // 토큰의 주체 (subject): 사용자 이메일 (username)
                .claim("auth", authorities) // 권한 정보 클레임 ("auth"로 통일)
                .setIssuedAt(new Date(now)) // 발행 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 토큰 서명
                .compact(); // JWT를 문자열로 직렬화
    }

    /**
     * Refresh Token을 생성합니다.
     * 토큰에는 사용자 ID(이메일)만 포함되며, 새 Access Token 재발급 용도로만 사용됩니다.
     *
     * @param authentication 현재 인증된 사용자 정보 (UserDetails를 구현하는 Principal)
     * @return 생성된 Refresh Token 문자열
     */
    public String generateRefreshToken(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        // ⭐ Refresh Token도 동일하게 UserDetails의 getUsername()을 Subject로 사용합니다. ⭐
        String subject = principal.getUsername();
        logger.info("[JwtTokenProvider] Refresh Token Subject 설정 (Username from UserDetails): {}", subject);

        long now = (new Date()).getTime();
        Date expiryDate = new Date(now + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(subject) // 토큰의 주체 (subject): 사용자 이메일 (username)
                // Refresh Token은 권한 정보를 포함하지 않는 것이 일반적 (보안 강화)
                .setIssuedAt(new Date(now)) // 발행 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 토큰 서명
                .compact(); // JWT를 문자열로 직렬화
    }

    /**
     * JWT 토큰에서 인증 정보를 추출합니다.
     * Access Token의 클레임(subject, auth)을 기반으로 Authentication 객체를 생성합니다.
     *
     * @param accessToken Access Token 문자열
     * @return Access Token으로부터 생성된 Authentication 객체
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            logger.error("Access Token에 'auth' 클레임(권한 정보)이 없습니다.");
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // ⭐ Spring Security의 UserDetails 구현체로 CustomUser 또는 CustomOAuth2User가 아닌,
        // 순수 Spring Security의 User 클래스를 사용하여 Authentication 객체를 만듭니다.
        // 이는 토큰으로부터 인증 정보를 재구성할 때 가장 간단하고 일반적인 방법입니다.
        // 나중에 필요하다면 CustomUserDetailsService를 통해 DB에서 User 엔티티를 다시 로드하여
        // 더 상세한 UserDetails(CustomUserDetails)를 Principal로 설정할 수 있습니다.
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                                claims.getSubject(), // JWT Subject (username/email)
                                "", // 토큰 기반 인증에서는 비밀번호가 필요 없으므로 빈 문자열
                                authorities);

        // 인증 완료된 UsernamePasswordAuthenticationToken 객체 반환
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * JWT 토큰의 유효성을 검사합니다 (서명 유효성, 만료 여부 등).
     * @param token JWT 문자열 (Access 또는 Refresh Token)
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 유효한 토큰
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명 또는 형식의 토큰입니다: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다: {}", e.getMessage());
            return false; // 만료된 토큰은 유효하지 않으므로 false 반환 (재발급 로직에서 활용)
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰 클레임이 비어있거나 잘못되었습니다: {}", e.getMessage());
        }
        return false; // 유효하지 않은 토큰
    }

    /**
     * JWT 토큰에서 Subject(사용자 이름)를 추출합니다.
     * @param token JWT 문자열
     * @return 토큰의 Subject 문자열 (사용자 이메일)
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * JWT 토큰에서 Claims 정보를 추출합니다 (만료된 토큰도 클레임 추출 가능).
     * @param token JWT 문자열
     * @return 토큰의 Claims 객체
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었더라도, subject 등 클레임 정보는 필요한 경우를 위해 반환
            return e.getClaims();
        }
    }

    // Access Token 만료 시간 getter (밀리초)
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    // Refresh Token 만료 시간 getter (밀리초)
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}