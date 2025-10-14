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
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtProperties jwtProps;
    private final AppProperties appProps;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOauth2UserDetails u = (CustomOauth2UserDetails) authentication.getPrincipal();

        // 1) 토큰 발급
        AuthTokens t = authService.issueFor(u.getUser());

        // 2) 다음 단계
        String nextStep = !u.isKycVerified() ? "KYC" : (!u.isDidVerified() ? "DID" : "HOME");

        // 3) Refresh 토큰을 HttpOnly 쿠키로 (도메인은 백엔드 운영 도메인 기준: chainee.store)
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

        // 4) 리다이렉트 대상: 무조건 localhost:3000로 가되,
        //    yml(app.frontend-baseurls)의 첫 값이 있으면 그걸 우선 사용.
        String base = resolveFrontendBase();              // 예: http://localhost:3000
        String cbPath = resolveCallbackPath();            // 예: /auth/callback
        String redirectBase = normalizeBase(base) + cbPath;

        // 5) Access 토큰/상태는 fragment(#)로 전달 (쿼리스트링에 남기지 않음)
        String redirectUrl = buildFragmentUrl(
                redirectBase,
                "accessToken", t.accessToken(),
                "accessExp", String.valueOf(t.accessExpEpochSec()),
                "refreshExp", String.valueOf(t.refreshExpEpochSec()),
                "nextStep", nextStep,
                "registered", String.valueOf(!u.isNewUser()),
                "email", u.getUsername()
        );

        // 6) 302 Redirect
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader("Location", redirectUrl);

        if (log.isDebugEnabled()) {
            log.debug("OAuth2 success -> redirect to {}", redirectUrl);
        }
    }

    /** 운영 도메인(chainee.store)에서만 Domain 지정, 로컬/기타는 null */
    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null;
    }

    /** 프론트 베이스 URL 선택: app.frontend-baseurls[0] → 없으면 http://localhost:3000 */
    private String resolveFrontendBase() {
        List<String> bases = appProps.effectiveBases();
        if (bases != null && !bases.isEmpty()) return bases.get(0);
        return "http://localhost:3000";
    }

    /** 콜백 경로: yml(app.oauth2.callback-path) → 없으면 /auth/callback */
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
