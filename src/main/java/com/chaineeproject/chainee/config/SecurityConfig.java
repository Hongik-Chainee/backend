package com.chaineeproject.chainee.config;

import com.chaineeproject.chainee.auth.KeyProvider;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.security.CustomOauth2UserService;
import com.chaineeproject.chainee.security.handler.CustomOAuth2FailureHandler;
import com.chaineeproject.chainee.security.handler.CustomOAuth2SuccessHandler;
import com.chaineeproject.chainee.security.oauth.CookieOAuth2AuthorizationRequestRepository;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class SecurityConfig {

    private final CustomOauth2UserService customOauth2UserService;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    public SecurityConfig(CustomOauth2UserService s, CustomOAuth2FailureHandler f, CustomOAuth2SuccessHandler ok) {
        this.customOauth2UserService = s;
        this.customOAuth2FailureHandler = f;
        this.customOAuth2SuccessHandler = ok;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CookieOAuth2AuthorizationRequestRepository cookieRepo) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Swagger / H2 / 로그인 등 공개
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/login", "/oauth2/**").permitAll()

                        // WS 핸드셰이크는 열어두고, STOMP CONNECT 에서 JWT 검증
                        .requestMatchers("/ws/**").permitAll()

                        // 인증 필요한 API
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/kyc/**").authenticated()
                        .requestMatchers("/api/did/**").authenticated()
                        .requestMatchers("/api/resumes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,   "/api/profile/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/profile/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/follow/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/follow/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/contracts/options/my-posts").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/notifications", "/api/notifications/unread-count").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/api/notifications/**").authenticated()

                        // ✅ 채팅 REST는 인증 필수
                        .requestMatchers("/api/chat/**").authenticated()

                        // 나머지
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(ae -> ae.authorizationRequestRepository(cookieRepo))
                        .loginPage("/login")
                        .userInfoEndpoint(u -> u.userService(customOauth2UserService))
                        .successHandler(customOAuth2SuccessHandler)
                        .failureHandler(customOAuth2FailureHandler)
                )

                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {})) // RS256 JWT 검증
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setContentType("application/json");
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write("{\"success\":false,\"messageCode\":\"UNAUTHORIZED\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setContentType("application/json");
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().write("{\"success\":false,\"messageCode\":\"FORBIDDEN\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyProvider keys, JwtProperties props) {
        try {
            RSAPublicKey publicKey = keys.currentKey().toRSAPublicKey();
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(props.issuer());
            decoder.setJwtValidator(withIssuer);
            return decoder;
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to extract RSA public key from JWK", e);
        }
    }

    @Bean
    public CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository() {
        return new CookieOAuth2AuthorizationRequestRepository("chainee.store");
    }

    @Bean
    public CorsFilter corsFilter(AppProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();
        for (String base : props.effectiveBases()) {
            cfg.addAllowedOrigin(base);
        }
        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("*");
        cfg.setAllowCredentials(true);
        cfg.addExposedHeader("Authorization");
        cfg.addExposedHeader("Set-Cookie");
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }
}
