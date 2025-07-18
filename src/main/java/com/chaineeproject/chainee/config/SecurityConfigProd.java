package com.chaineeproject.chainee.config;

import com.chaineeproject.chainee.security.handler.CustomOAuth2FailureHandler;
import com.chaineeproject.chainee.service.CustomOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class SecurityConfigProd {

    private final CustomOauth2UserService customOauth2UserService;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @Bean
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOauth2UserService))
                        .failureHandler(customOAuth2FailureHandler)
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
