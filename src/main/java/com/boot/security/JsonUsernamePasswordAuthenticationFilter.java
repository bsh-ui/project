package com.boot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Spring Security의 기본 UsernamePasswordAuthenticationFilter를 확장하여
 * application/json 형태의 로그인 요청을 처리합니다.
 */
@Slf4j
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // ⭐ 중요: 이 필터가 처리할 로그인 URL을 설정합니다.
        // SecurityConfig의 formLogin().loginProcessingUrl()과 일치해야 합니다.
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        // HTTP 메서드가 POST가 아니면 AuthenticationServiceException 발생
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        // Content-Type이 JSON이 아니면 기존 폼 파싱 로직 사용 (선택 사항, 여기서는 JSON만 처리)
        // String contentType = request.getHeader("Content-Type");
        // if (contentType == null || !contentType.contains("application/json")) {
        //     log.warn("⚠️ [JsonUsernamePasswordAuthenticationFilter] Content-Type is not application/json. Falling back to default form parsing.");
        //     return super.attemptAuthentication(request, response);
        // }

        try {
            // 요청 본문을 Map으로 읽어옵니다. (JSON 파싱)
            Map<String, String> loginRequest = objectMapper.readValue(request.getInputStream(), Map.class);
            String username = loginRequest.get(getUsernameParameter()); // "username" 파라미터 값 가져오기
            String password = loginRequest.get(getPasswordParameter()); // "password" 파라미터 값 가져오기

            // 로그 추가: JSON에서 파싱된 username과 password 확인
            log.info("🔍 [JsonUsernamePasswordAuthenticationFilter] Parsed username from JSON: {}", username);
            // 비밀번호는 로깅하지 않는 것이 좋지만, 디버깅을 위해 임시로 추가
            // log.info("🔍 [JsonUsernamePasswordAuthenticationFilter] Parsed password from JSON: {}", password);

            if (username == null) {
                username = "";
            }
            if (password == null) {
                password = "";
            }

            username = username.trim(); // 공백 제거

            // UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    username, password);

            // 요청에 대한 세부 정보 설정
            setDetails(request, authRequest);

            // AuthenticationManager를 통해 인증 시도
            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            log.error("🚫 [JsonUsernamePasswordAuthenticationFilter] Error parsing JSON login request: {}", e.getMessage(), e);
            throw new AuthenticationServiceException("Failed to parse authentication request body", e);
        }
    }
}
