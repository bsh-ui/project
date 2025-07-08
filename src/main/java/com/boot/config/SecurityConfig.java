package com.boot.config;

import com.boot.jwt.JwtAuthenticationFilter;
<<<<<<< HEAD
=======
import com.boot.jwt.JwtTokenProvider;
>>>>>>> main
import com.boot.oauth2.CustomFormSuccessHandler;
import com.boot.oauth2.CustomLogoutSuccessHandler;
import com.boot.oauth2.CustomOAuth2Success;
import com.boot.security.CustomAuthenticationFailureHandler;
import com.boot.security.CustomOAuth2UserService;
<<<<<<< HEAD
=======
import com.boot.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;

>>>>>>> main
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
<<<<<<< HEAD
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
=======
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // ⭐ 이 줄이 있어야 합니다 ⭐
>>>>>>> main
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
<<<<<<< HEAD
=======
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; // ⭐ 이 줄이 있어야 합니다 ⭐
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
>>>>>>> main
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
<<<<<<< HEAD

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
=======
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ⭐ 추가: 메서드 보안 활성화를 위해 필요합니다. ⭐
>>>>>>> main
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2Success customOAuth2Success;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
<<<<<<< HEAD
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomFormSuccessHandler customFormSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager()
        		;
        
=======
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomFormSuccessHandler customFormSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService customUserDetailsService;



    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        return new ProviderManager(Collections.singletonList(authenticationProvider));
    }

    // JsonUsernamePasswordAuthenticationFilter 빈 정의는 현재 Thymeleaf/JSP 환경에서는 필요 없으므로 제거된 상태를 유지합니다.
    // @Bean
    // public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter() {
    //     JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
    //     filter.setAuthenticationManager(authenticationManager());
    //     filter.setAuthenticationSuccessHandler(customFormSuccessHandler);
    //     filter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);
    //     filter.setFilterProcessesUrl("/api/login");
    //     return filter;
    // }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8485"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("success", false);
            errorDetails.put("message", "인증이 필요하거나 유효한 토큰이 없습니다.");
            errorDetails.put("timestamp", System.currentTimeMillis());

            try (PrintWriter writer = response.getWriter()) {
                objectMapper.writeValue(writer, errorDetails);
            }
        };
>>>>>>> main
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);

        http
<<<<<<< HEAD
        	.cors(cors -> {})
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz

                // 🔒 ADMIN 전용
                .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/notices")).hasRole("ADMIN")
                .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/notices/**")).hasRole("ADMIN")
                .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/notices/**")).hasRole("ADMIN")
                .requestMatchers(mvc.pattern("/notice_admin")).hasRole("ADMIN")

                // 🔐 인증된 사용자
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/music/stream/{id}")).authenticated()
                .requestMatchers(mvc.pattern("/api/auth/me")).authenticated()
                .requestMatchers(mvc.pattern("/api/userinfo")).authenticated()
                .requestMatchers(mvc.pattern("/mypage")).authenticated()
                .requestMatchers(mvc.pattern("/api/mypage/**")).authenticated()

                // 📄 게시글/댓글/플레이리스트 수정은 인증 필요
                .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/posts")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/posts/**")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/posts/**")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/posts/*/comments")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/posts/*/comments/**")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/posts/*/likes")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/posts/*/dislikes")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/playlists")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/playlists/**")).authenticated()
                .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/playlists/**")).authenticated()

                // 🌐 공개 접근 가능 (정적 자원 + 일부 API)
                .requestMatchers(
                    mvc.pattern("/"),
                    mvc.pattern("/main"),
                    mvc.pattern("/main/**"),
                    mvc.pattern("/board"),
                    mvc.pattern("/login"),
                    mvc.pattern("/custom_login/**"),
                    mvc.pattern("/favicon.ico"),
                    mvc.pattern("/manifest.json"),
                    mvc.pattern("/logo*.png"),
                    mvc.pattern("/asset-manifest.json"),
                    mvc.pattern("/robots.txt"),
                    mvc.pattern("/*.jpg"),
                    mvc.pattern("/*.png"),
                    mvc.pattern("/css/**"),
                    mvc.pattern("/js/**"),
                    mvc.pattern("/images/**"),
                    mvc.pattern("/static/**"),
                    mvc.pattern("/uploads/**")
                ).permitAll()

                .requestMatchers(
                    mvc.pattern("/api/login"),
                    mvc.pattern("/api/auth/signup"),
                    mvc.pattern("/signup"),
                    mvc.pattern("/forgot-password"),
                    mvc.pattern("/api/refresh-token")
                ).permitAll()

                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/music")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/music/{id}")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/posts")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/posts/**")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/comments")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/comments/**")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/playlists")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/playlists/**")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/notices")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/notices/**")).permitAll()
                .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/files/cover-image/**")).permitAll()

                // 🛑 그 외 모든 요청 인증 필요
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/custom_login")
                .loginProcessingUrl("/api/login")
                .successHandler(customFormSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/custom_login")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customOAuth2Success)
            )
            .logout(logout -> logout
                .logoutUrl("/log_out")
                .logoutSuccessUrl("/main")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "OAuth_Token", "jwt_token", "refresh_token")
                .permitAll()
            )
            .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self' http://localhost:8485 https://www.gstatic.com;" +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval' http://localhost:8485 https://cdnjs.cloudflare.com https://code.jquery.com https://cdn.jsdelivr.net https://www.gstatic.com;" +
                "style-src 'self' 'unsafe-inline' http://localhost:8485 https://cdnjs.cloudflare.com https://stackpath.bootstrapcdn.com https://www.gstatic.com;" +
                "img-src 'self' data: https://ssl.pstatic.net https://k.kakaocdn.net;" +
                "connect-src 'self' http://localhost:8485;" +
                "font-src 'self' data: https://cdnjs.cloudflare.com;" +
                "media-src 'self' blob: http://localhost:8485;" +
                "object-src 'none';" +
                "base-uri 'self';"
            ))
            .frameOptions(frameOptions -> frameOptions.deny())
            .contentTypeOptions(options -> options.disable())
            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)));
=======
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(authz -> authz
                        // Public Endpoints (정적 자원 및 Thymeleaf/JSP 뷰)
                        .requestMatchers(
                            mvc.pattern("/"),
                            mvc.pattern("/index.html"),
                            mvc.pattern("/main"),
                            mvc.pattern("/board"),
                            mvc.pattern("/login"),
                            mvc.pattern("/signup"),
                            mvc.pattern("/forgot-password"),
                            mvc.pattern("/music/{id}"),
                            mvc.pattern("/notice-list"),
                            mvc.pattern("/notice-detail/{id}"),
                            mvc.pattern("/posts"),
                            mvc.pattern("/posts/{id}"),
                            mvc.pattern("/posts/new"),
                            mvc.pattern("/posts/edit/{id}"),
                            mvc.pattern("/profile"),
                            mvc.pattern("/my-playlists"),
                            mvc.pattern("/admin/music"),
                            mvc.pattern("/admin/notice"),
                            mvc.pattern("/custom_login"),
                            mvc.pattern("/error"),
                            mvc.pattern("/swagger-ui/**"),
                            mvc.pattern("/v3/api-docs/**"),
                            mvc.pattern("/css/**"),
                            mvc.pattern("/js/**"),
                            mvc.pattern("/images/**"),
                            mvc.pattern("/static/**"),
                            mvc.pattern("/uploads/**"), // 업로드된 파일 접근
                            mvc.pattern("/favicon.ico"),
                            mvc.pattern("/manifest.json"),
                            mvc.pattern("/logo*.png"),
                            mvc.pattern("/asset-manifest.json"),
                            mvc.pattern("/robots.txt"),
                            mvc.pattern("/*.jpg"),
                            mvc.pattern("/*.png")
                        ).permitAll()
                        // Public API Endpoints
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/check-username").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/check-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/check-nickname").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/refresh-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/find-username").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/music").permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/music/{id}")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/music/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/posts/{id}")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/{id}/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments").permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/comments/{id}")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/playlists").permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/playlists/{id}")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/notices").permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/notices/{id}")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/files/cover-image/{id}")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/music/stream/{id}")).permitAll()

                        // ADMIN 권한 필요
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notices/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/{id}").hasRole("ADMIN")
                        .requestMatchers("/notice_admin").hasRole("ADMIN")

                        // 인증된 사용자 필요
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/userinfo").authenticated()
                        .requestMatchers("/mypage").authenticated()
                        .requestMatchers(mvc.pattern("/api/mypage/**")).authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/user/update").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/user/delete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/change-password").authenticated()

                        // 게시글/댓글/플레이리스트 수정/삭제 등
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/{id}/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/{postId}/comments/{commentId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/{postId}/comments/{commentId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/{id}/likes").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/{id}/dislikes").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/playlists").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/playlists/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/playlists/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/playlists/{playlistId}/music/{musicId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/playlists/{playlistId}/music/{musicId}").authenticated()

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form.loginPage("/custom_login")
                        .loginProcessingUrl("/api/login")
                        .successHandler(customFormSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )

                .oauth2Login(oauth2 -> oauth2.loginPage("/custom_login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(customOAuth2Success)
                )

                .logout(logout -> logout.logoutUrl("/log_out")
                        .logoutSuccessUrl("/main")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "OAuth_Token", "jwt_token", "refresh_token")
                        .permitAll()
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint())
                )

                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self' http://localhost:8485 http://localhost:3000 https://www.gstatic.com;" +
                                "script-src 'self' 'unsafe-inline' 'unsafe-eval' http://localhost:8485 http://localhost:3000 https://cdnjs.cloudflare.com https://code.jquery.com https://cdn.jsdelivr.net https://www.gstatic.com;" +
                                "style-src 'self' 'unsafe-inline' http://localhost:8485 http://localhost:3000 https://cdnjs.cloudflare.com https://stackpath.bootstrapcdn.com;" +
                                "img-src 'self' data: https://ssl.pstatic.net https://k.kakaocdn.net http://localhost:8485 http://localhost:3000;" +
                                "connect-src 'self' http://localhost:8485 http://localhost:3000;" +
                                "font-src 'self' data: https://cdnjs.cloudflare.com;" +
                                "media-src 'self' blob: http://localhost:8485 http://localhost:3000;" +
                                "object-src 'none';" +
                                "base-uri 'self';"
                        ))
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentTypeOptions(options -> options.disable())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                );
>>>>>>> main

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> main
