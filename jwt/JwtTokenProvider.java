package com.boot.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException; // 만료된 토큰 예외
import io.jsonwebtoken.MalformedJwtException; // 잘못된 형식 토큰 예외
import io.jsonwebtoken.UnsupportedJwtException; // 지원되지 않는 토큰 예외

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger; // 로깅을 위한 import
import org.slf4j.LoggerFactory; // 로깅을 위한 import

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component // Spring 빈으로 등록
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class); // 로거 인스턴스

    // JWT 서명 비밀 키 (application.properties에서 주입)
    // 보안을 위해 강력하고 랜덤한 256비트(32바이트) 이상의 문자열 사용 권장
    @Value("${jwt.secret-key}") // 기존 secret-key 사용
    private String secretKeyString; // 필드명 변경

    // Access Token 만료 시간 (application.properties에서 주입, 밀리초)
    @Value("${jwt.access-token-expiration}") // 새로 추가될 속성명
    private long accessTokenExpiration;

    // Refresh Token 만료 시간 (application.properties에서 주입, 밀리초)
    @Value("${jwt.refresh-token-expiration}") // 새로 추가될 속성명
    private long refreshTokenExpiration;

    // 실제 JWT 서명에 사용될 Key 객체
    private Key key;

    // 빈 초기화 시 비밀 키로부터 Key 객체 생성
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        // logger.info("JWT Secret Key initialized."); // 디버그용
    }

    /**
     * Access Token 생성
     * 토큰에 사용자 ID와 권한 정보 포함 (실제 리소스 접근에 사용)
     * @param authentication 현재 인증된 사용자 정보
     * @return 생성된 Access Token 문자열
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName(); // 사용자 ID (Principal)
        // 사용자의 권한들을 쉼표로 구분된 문자열로 변환하여 클레임에 추가
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date expiryDate = new Date(now + accessTokenExpiration); // Access Token 만료 시간 적용

        return Jwts.builder()
                .setSubject(username) // 토큰의 주체 (subject): 사용자 ID
                .claim("auth", authorities) // 권한 정보 클레임 ("auth"로 통일)
                .setIssuedAt(new Date(now)) // 발행 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 토큰 서명
                .compact(); // JWT를 문자열로 직렬화
    }

    /**
     * Refresh Token 생성
     * 토큰에 사용자 ID만 포함 (새 Access Token 재발급 용도로만 사용)
     * @param authentication 현재 인증된 사용자 정보
     * @return 생성된 Refresh Token 문자열
     */
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName(); // 사용자 ID (Principal)

        long now = (new Date()).getTime();
        Date expiryDate = new Date(now + refreshTokenExpiration); // Refresh Token 만료 시간 적용

        return Jwts.builder()
                .setSubject(username) // 토큰의 주체 (subject): 사용자 ID
                // Refresh Token은 권한 정보를 포함하지 않는 것이 일반적 (보안 강화)
                .setIssuedAt(new Date(now)) // 발행 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 토큰 서명
                .compact(); // JWT를 문자열로 직렬화
    }

    /**
     * Access Token에서 인증 정보 추출
     * Access Token의 클레임(subject, auth)을 기반으로 Authentication 객체 생성
     * @param accessToken Access Token 문자열
     * @return Access Token으로부터 생성된 Authentication 객체
     */
    public Authentication getAuthentication(String accessToken) {
        // 토큰 파싱 및 서명 검증. 만료된 토큰도 Claims는 추출되도록 parseClaims 사용.
        Claims claims = parseClaims(accessToken);

        // Access Token에는 "auth" 클레임(권한 정보)이 필수
        if (claims.get("auth") == null) {
            logger.error("Access Token에 'auth' 클레임(권한 정보)이 없습니다.");
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // "auth" 클레임에서 권한 정보를 추출하여 GrantedAuthority 객체 리스트로 변환
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // Spring Security의 UserDetails 객체 생성 (사용자 이름과 권한만 필요, 비밀번호는 빈 문자열)
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        // 인증 완료된 UsernamePasswordAuthenticationToken 객체 반환
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * JWT 토큰의 유효성 검사 (서명 유효성, 만료 여부 등)
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
     * JWT 토큰에서 Claims 정보 추출 (만료된 토큰도 클레임 추출 가능)
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