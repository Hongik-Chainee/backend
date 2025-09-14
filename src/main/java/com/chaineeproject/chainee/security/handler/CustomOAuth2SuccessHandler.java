package com.chaineeproject.chainee.security.handler;

import com.chaineeproject.chainee.security.CustomOauth2UserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOauth2UserDetails u = (CustomOauth2UserDetails) authentication.getPrincipal();

        boolean kycDone = u.isKycVerified();
        boolean didDone = u.isDidVerified();

        String nextStep;
        if (!kycDone) nextStep = "KYC";
        else if (!didDone) nextStep = "DID";
        else nextStep = "HOME";

        Map<String, Object> body = new HashMap<>();
        body.put("authenticated", true);
        body.put("registered", !u.isNewUser()); // 유지(첫 로그인 식별)
        body.put("email", u.getUsername());
        body.put("status", Map.of("kyc", kycDone, "did", didDone));
        body.put("nextStep", nextStep);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}
