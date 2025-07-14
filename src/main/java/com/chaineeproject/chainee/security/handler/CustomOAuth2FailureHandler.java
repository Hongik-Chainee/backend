package com.chaineeproject.chainee.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        log.warn("OAuth2 로그인 실패: {}", exception.getMessage());

        String messageCode = "UNKNOWN_ERROR";
        boolean userExists = false;

        if (exception instanceof OAuth2AuthenticationException oae) {
            messageCode = oae.getError().getErrorCode();

            if ("EMAIL_EXISTS".equals(messageCode)) {
                userExists = true;
            } else if ("EMAIL_AUTH_CODE_SENT".equals(messageCode)) {
                userExists = false;
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("messageCode", messageCode);
        body.put("data", Map.of("userExists", userExists));

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK); // 200 응답 유지
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
