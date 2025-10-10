// src/main/java/com/chaineeproject/chainee/security/handler/CustomOAuth2SuccessHandler.java
package com.chaineeproject.chainee.security.handler;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.auth.AuthTokens;
import com.chaineeproject.chainee.config.AppProperties;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.security.CustomOauth2UserDetails;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;   // Access/Refresh 발급
    private final JwtProperties jwtProps;    // TTL/쿠키 정책
    private final AppProperties appProps;    // 프론트 콜백 URL 구성

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOauth2UserDetails u = (CustomOauth2UserDetails) authentication.getPrincipal();

        // 1) 토큰 한 번에 발급
        AuthTokens t = authService.issueFor(u.getUser());

        // 2) 다음 단계 결정
        String nextStep = !u.isKycVerified() ? "KYC" : (!u.isDidVerified() ? "DID" : "HOME");

        // 3) Refresh 토큰은 HttpOnly 쿠키로 내려서 JS에서 읽히지 않게
        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        boolean secure  = Boolean.TRUE.equals(jwtProps.cookie().secure());
        String sameSite = jwtProps.cookie().sameSite();
        String cookieDomain = cookieDomainFor(request);
        CookieUtil.addCookie(
                response,
                jwtProps.refreshCookieName(),
                t.refreshToken(),
                cookieDomain,
                true,                // HttpOnly
                secure,              // Secure
                sameSite,            // e.g., Lax / None
                maxAge
        );

        // 4) Access 토큰/상태는 URL fragment(#)로 전달 → 서버/프록시 로그에 남지 않음
        //    예) https://app.chainee.store/auth/callback#accessToken=...&accessExp=...&nextStep=KYC&registered=true&email=...
        String base = normalizeBase(appProps.frontendBaseUrl()) + appProps.oauth2().callbackPath();
        String redirectUrl = buildFragmentUrl(
                base,
                "accessToken", t.accessToken(),
                "accessExp", String.valueOf(t.accessExpEpochSec()),
                "refreshExp", String.valueOf(t.refreshExpEpochSec()),
                "nextStep", nextStep,
                "registered", String.valueOf(!u.isNewUser()),
                "email", u.getUsername()
        );

        response.setStatus(HttpStatus.FOUND.value());   // 302
        response.setHeader("Location", redirectUrl);
        // 바디 없음
    }

    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store"; // 운영에서만 Domain 지정
        }
        return null; // dev/local (localhost/127.0.0.1 등) 에서는 Domain 미지정
    }

    private String normalizeBase(String base) {
        if (base == null || base.isBlank()) return "/";
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** fragment에 key=value&key=value 형태로 안전하게 넣는다 */
    private String buildFragmentUrl(String base, String... kv) {
        StringBuilder sb = new StringBuilder(base);
        sb.append("#");
        for (int i = 0; i < kv.length; i += 2) {
            if (i > 0) sb.append("&");
            sb.append(enc(kv[i])).append("=").append(enc(kv[i + 1]));
        }
        return sb.toString();
    }
}
