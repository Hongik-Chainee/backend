package com.chaineeproject.chainee.config;

import com.chaineeproject.chainee.security.handler.CustomOAuth2FailureHandler;
import com.chaineeproject.chainee.security.handler.CustomOAuth2SuccessHandler;
import com.chaineeproject.chainee.security.CustomOauth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOauth2UserService customOauth2UserService;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})                      // CORS는 아래 CorsFilter Bean 사용
                .csrf(csrf -> csrf.disable())          // API 용도: CSRF 비활성
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // H2 콘솔
                .authorizeHttpRequests(auth -> auth
                        // 1) 공개 리소스
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/login", "/oauth2/**").permitAll()
                        .requestMatchers("/dev/mock-login").permitAll()

                        // 2) 인증 필수 API (순서 중요: 광범위 /api/** 보다 위에 둔다)
                        .requestMatchers("/api/kyc/**").authenticated()
                        .requestMatchers("/api/did/**").authenticated()
                        .requestMatchers("/api/resumes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,   "/api/profile/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/profile/**").authenticated()

                        // 3) 그 외 API는 현행 유지(원하면 이후 점진적으로 tighten)
                        .requestMatchers("/api/**").permitAll()

                        // 4) 나머지 전부 허용(정적/리다이렉트 등)
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOauth2UserService))
                        .successHandler(customOAuth2SuccessHandler)
                        .failureHandler(customOAuth2FailureHandler)
                )
                .exceptionHandling(exception -> exception
                        // 인증 필요 자원 접근 시 401 JSON
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"success\":false,\"messageCode\":\"UNAUTHORIZED\"}");
                        })
                        // 권한 부족 시 403 JSON (optional)
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"success\":false,\"messageCode\":\"FORBIDDEN\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 프론트 도메인/포트 추가
        cfg.addAllowedOriginPattern("http://localhost:3000");
        cfg.addAllowedOriginPattern("http://localhost:5173");
        cfg.addAllowedOriginPattern("https://chainee.store");
        cfg.addAllowedOriginPattern("https://www.chainee.store");

        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("*");
        cfg.setAllowCredentials(true);
        // 프론트에서 쿠키/토큰 확인 시 노출 필요하면 헤더 노출
        cfg.addExposedHeader("Authorization");
        cfg.addExposedHeader("Set-Cookie");
        cfg.setMaxAge(3600L); // preflight 캐시

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }
}
