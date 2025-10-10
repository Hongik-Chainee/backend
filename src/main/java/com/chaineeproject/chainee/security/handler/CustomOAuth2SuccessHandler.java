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

        // 3) Refresh 토큰을 HttpOnly 쿠키로
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

        // 4) 리다이렉트 대상 베이스 URL 자동 선택 (Origin/Referer와 yml 목록 비교)
        String base = normalizeBase(chooseBase(request)) + appProps.oauth2().callbackPath();

        // 5) Access 토큰/상태는 fragment로 전달
        String redirectUrl = buildFragmentUrl(
                base,
                "accessToken", t.accessToken(),
                "accessExp", String.valueOf(t.accessExpEpochSec()),
                "refreshExp", String.valueOf(t.refreshExpEpochSec()),
                "nextStep", nextStep,
                "registered", String.valueOf(!u.isNewUser()),
                "email", u.getUsername()
        );

        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader("Location", redirectUrl);
    }

    /** 요청 호스트가 운영 도메인(chainee.store)이면 Domain 지정, 아니면 로컬/개발은 null */
    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null;
    }

    /** Origin/Referer와 yml의 허용 목록을 비교하여 베이스 URL 선택 */
    private String chooseBase(HttpServletRequest req) {
        String origin  = req.getHeader("Origin");
        String referer = req.getHeader("Referer");
        List<String> bases = appProps.effectiveBases();

        for (String base : bases) {
            if (origin != null && origin.startsWith(base))  return base;
            if (referer != null && referer.startsWith(base)) return base;
        }
        // 매칭 실패 시 첫 번째, 없으면 빈 문자열
        return bases.isEmpty() ? "" : bases.get(0);
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
