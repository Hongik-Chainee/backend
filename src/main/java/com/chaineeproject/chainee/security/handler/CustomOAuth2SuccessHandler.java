package com.chaineeproject.chainee.security.handler;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.auth.AuthTokens;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.security.CustomOauth2UserDetails;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;     // ✅ TokenService 대신 AuthService
    private final JwtProperties jwtProps;      // ✅ TTL/쿠키 정책은 yml에서
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOauth2UserDetails u = (CustomOauth2UserDetails) authentication.getPrincipal();

        // 1) Access/Refresh 한 번에 발급 + Refresh 해시 저장(DB)
        AuthTokens t = authService.issueFor(u.getUser());

        // 2) 단계 안내
        String nextStep = !u.isKycVerified() ? "KYC" : (!u.isDidVerified() ? "DID" : "HOME");

        // 3) Refresh 쿠키 설정 (도메인 분기: 로컬= null, 운영= chainee.store)
        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        boolean secure = Boolean.TRUE.equals(jwtProps.cookie().secure());
        String sameSite = jwtProps.cookie().sameSite();
        String cookieDomain = cookieDomainFor(request);
        CookieUtil.addCookie(
                response,
                jwtProps.refreshCookieName(),
                t.refreshToken(),
                cookieDomain,
                true,               // HttpOnly
                secure,             // Secure
                sameSite,           // Lax/None 등
                maxAge
        );

        // 4) 응답 바디 (SPA가 accessToken 저장/사용)
        var body = Map.of(
                "authenticated", true,
                "registered", !u.isNewUser(),
                "email", u.getUsername(),
                "status", Map.of("kyc", u.isKycVerified(), "did", u.isDidVerified()),
                "nextStep", nextStep,
                "accessToken", t.accessToken(),
                "accessExp", t.accessExpEpochSec(),
                "refreshExp", t.refreshExpEpochSec()
        );

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(om.writeValueAsString(body));
    }

    /** 운영 도메인에서만 Domain=chainee.store 지정, 로컬/개발은 null로 둬야 쿠키가 저장됨 */
    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null; // localhost/127.0.0.1 등 개발환경
    }
}
