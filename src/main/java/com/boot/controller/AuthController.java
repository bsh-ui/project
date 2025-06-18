package com.boot.controller;

import com.boot.domain.User;
import com.boot.dto.UserDTO;
import com.boot.jwt.JwtTokenProvider;
import com.boot.service.UserService;
import com.boot.dto.SignUpRequestDTO; // 회원가입 DTO 추가

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException; // AuthenticationException 임포트 추가
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

// Login 요청을 위한 DTO (내부 클래스로 정의)
record LoginRequest(String username, String password) {}

// Login 응답을 위한 DTO (내부 클래스로 정의)
record LoginResponse(String accessToken, UserDTO user) {}


@RestController
@RequestMapping("/api/auth") // ⭐ API 기본 URL 경로를 /api/auth로 변경 ⭐
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    /**
     * 회원가입 처리 API
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO) { // @Valid는 DTO에 @Email, @NotBlank 등이 있을 때 사용
        log.info("회원가입 요청 수신: {}", signUpRequestDTO.getEmail());
        try {
            UserDTO registeredUser = userService.registerNewUser(signUpRequestDTO);
            log.info("회원가입 성공: {}", registeredUser.getUsername());
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패 (비즈니스 로직 오류): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 실패 (서버 오류): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 중 오류가 발생했습니다.");
        }
    }

    /**
     * React 앱에서 JSON으로 로그인 요청을 처리하고 JWT를 응답 본문에 반환합니다.
     * 또한, HttpOnly(false) 쿠키로 Access Token을, HttpOnly(true) 쿠키로 Refresh Token을 발행합니다.
     * POST /api/auth/authenticate
     *
     * @param request LoginRequest DTO (username, password)
     * @param response HttpServletResponse (쿠키 추가용)
     * @return 로그인 성공 시 JWT 및 UserDTO를 포함한 LoginResponse
     */
    @PostMapping("/authenticate") // ⭐ 로그인 엔드포인트 변경 및 이름 변경 ⭐
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest request, HttpServletResponse response) {
        log.info("📧 [AuthController] 인증 요청 수신: {}", request.username());
        try {
            // UsernamePasswordAuthenticationToken을 사용하여 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            // SecurityContextHolder에 인증된 Authentication 객체 설정 (선택적이지만 @AuthenticationPrincipal을 위해 유지)
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("✅ [AuthController] 사용자 인증 성공: {}", authentication.getName());

            // UserDetails (User 엔티티) 객체 가져오기
            User userEntity = (User) authentication.getPrincipal();

            // JWT 토큰 생성
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            log.info("🔑 [AuthController] Access Token 생성 완료.");
            log.info("🔑 [AuthController] Refresh Token 생성 완료.");

            // Refresh Token을 HttpOnly 쿠키로 추가 (보안상 권장)
            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
            response.addCookie(refreshTokenCookie);
            log.info("✅ [AuthController] Refresh Token을 HttpOnly 쿠키로 추가 완료");

            // Access Token을 HttpOnly(false) 쿠키로 추가 (React에서 읽을 수 있도록)
            Cookie accessTokenCookie = new Cookie("jwt_token", accessToken);
            accessTokenCookie.setHttpOnly(false); // ⭐ JS에서 접근 가능하도록 설정 ⭐
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000));
            response.addCookie(accessTokenCookie);
            log.info("✅ [AuthController] Access Token을 HttpOnly(false) 쿠키 'jwt_token'으로 추가 완료");

            // UserDTO로 변환하여 반환
            UserDTO userDTO = userEntity.toDTO();
            log.info("✅ [AuthController] 사용자 DTO 생성 완료: {}", userDTO.getNickname());

            // 로그인 성공 응답 (JWT Access Token과 UserDTO 포함)
            return ResponseEntity.ok(new LoginResponse(accessToken, userDTO));

        } catch (AuthenticationException e) { // 인증 실패 시 AuthenticationException
            log.warn("⚠️ [AuthController] 인증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(null, null)); // 또는 오류 메시지를 담은 응답
        } catch (Exception e) {
            log.error("⚠️ [AuthController] 로그인 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponse(null, null));
        }
    }

    /**
     * 현재 인증된 사용자 정보를 반환합니다.
     * 이 API는 프론트엔드에서 페이지 로드 시 로그인 상태를 확인하고 사용자 정보를 가져오는 데 사용됩니다.
     * JWT 토큰은 HttpOnly(false) 쿠키 또는 Authorization 헤더를 통해 전송됩니다.
     * GET /api/auth/me
     *
     * @param currentUser 현재 인증된 사용자 (Spring Security로부터 주입)
     * @return UserDTO 형태의 사용자 정보
     */
    @GetMapping("/me") // ⭐ 사용자 정보 조회 엔드포인트 변경 ⭐
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        log.info("👤 [AuthController] /me 요청 수신, 인증된 사용자: {}", currentUser != null ? currentUser.getUsername() : "없음");
        if (currentUser == null) {
            // 인증되지 않은 사용자라면 401 Unauthorized 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // User 엔티티를 UserDTO로 변환하여 반환
        return ResponseEntity.ok(currentUser.toDTO());
    }
}
