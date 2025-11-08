// src/main/java/com/chaineeproject/chainee/security/handler/CustomOAuth2SuccessHandler.java
package com.chaineeproject.chainee.security.handler;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.auth.AuthTokens;
import com.chaineeproject.chainee.auth.LoginCodeStore;   // ✅ 추가
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
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtProperties jwtProps;
    private final AppProperties appProps;
    private final LoginCodeStore loginCodeStore; // ✅ 주입

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOauth2UserDetails u = (CustomOauth2UserDetails) authentication.getPrincipal();

        // 1) 토큰 발급 (refresh는 쿠키로, access는 교환 API에서 내려줄 예정)
        AuthTokens t = authService.issueFor(u.getUser());

        // 2) 다음 스텝
        String nextStep = !u.isKycVerified() ? "KYC" : (!u.isDidVerified() ? "DID" : "HOME");

        // 3) refresh 토큰을 쿠키에 저장
        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        boolean secure  = Boolean.TRUE.equals(jwtProps.cookie().secure());
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

        // 4) 1회용 loginCode 생성/저장 (유효 60초 권장)
        String loginCode = UUID.randomUUID().toString().replace("-", "");
        loginCodeStore.put(loginCode, u.getUser().getId(), Duration.ofSeconds(60));

        // 5) 프론트 콜백으로 리다이렉트: fragment에 loginCode만 전달
        String base = resolveFrontendBase();
        String cbPath = resolveCallbackPath();
        String redirectBase = normalizeBase(base) + cbPath;

        String redirectUrl = buildFragmentUrl(
                redirectBase,
                "loginCode", loginCode,
                "nextStep", nextStep,
                "registered", String.valueOf(!u.isNewUser()),
                "email", u.getUsername()
        );

        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader("Location", redirectUrl);

        if (log.isDebugEnabled()) {
            log.debug("OAuth2 success -> redirect to {}", redirectUrl);
        }
    }

    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null;
    }

    private String resolveFrontendBase() {
        List<String> bases = appProps.effectiveBases();
        if (bases != null && !bases.isEmpty()) return bases.get(0);
        return "http://localhost:3000";
    }

    private String resolveCallbackPath() {
        return Optional.ofNullable(appProps.oauth2())
                .map(AppProperties.Oauth2Props::callbackPath)
                .filter(p -> !p.isBlank())
                .orElse("/auth/callback");
    }

    private String normalizeBase(String base) {
        if (base == null || base.isBlank()) return "/";
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private String buildFragmentUrl(String base, String... kv) {
        StringBuilder sb = new StringBuilder(base).append("#");
        for (int i = 0; i < kv.length; i += 2) {
            if (i > 0) sb.append("&");
            sb.append(enc(kv[i])).append("=").append(enc(kv[i + 1]));
        }
        return sb.toString();
    }
}
