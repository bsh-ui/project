package com.boot.config;

import com.boot.jwt.JwtAuthenticationFilter;
import com.boot.oauth2.CustomFormSuccessHandler;
import com.boot.oauth2.CustomLogoutSuccessHandler;
import com.boot.oauth2.CustomOAuth2Success;
import com.boot.security.CustomAuthenticationFailureHandler;
import com.boot.security.CustomOAuth2UserService;
import com.boot.security.JsonUsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2Success customOAuth2Success;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomFormSuccessHandler customFormSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final ObjectMapper objectMapper;
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager()
        		;
        
    }
    // ⭐ JsonUsernamePasswordAuthenticationFilter 빈 등록 ⭐
    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager) throws Exception { // CustomFormSuccessHandler 주입은 생성자 주입으로
        JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(customFormSuccessHandler);
        filter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);
        filter.setFilterProcessesUrl("/api/login");
        return filter;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8485"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);

        http
        	.cors(cors -> cors.configurationSource(corsConfigurationSource()))
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

//            .formLogin(form -> form
//                .loginPage("/custom_login")
//                .loginProcessingUrl("/api/login")
//                .successHandler(customFormSuccessHandler)
//                .failureHandler(customAuthenticationFailureHandler)
//                .permitAll()
//               
//            )
            .requestCache(AbstractHttpConfigurer::disable)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/custom_login")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customOAuth2Success)
            )
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessUrl("/login")
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

        http.addFilterAt(jsonUsernamePasswordAuthenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class))), UsernamePasswordAuthenticationFilter.class);
        
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}