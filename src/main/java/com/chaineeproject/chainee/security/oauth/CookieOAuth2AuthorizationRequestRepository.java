package com.chaineeproject.chainee.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.Base64;

public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "OAUTH2_AUTHZ_REQ";
    private static final int   MAX_AGE_SEC  = 600;

    /** 운영 도메인(예: chainee.store). 로컬/개발에서는 자동으로 null 처리 */
    private final String prodDomain;

    public CookieOAuth2AuthorizationRequestRepository(String prodDomain) {
        this.prodDomain = prodDomain;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        var c = CookieUtil.getCookie(request, COOKIE_NAME);
        if (c == null) return null;
        var bytes = Base64.getUrlDecoder().decode(c.getValue());
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }
        var bytes = SerializationUtils.serialize(authorizationRequest);
        var value = Base64.getUrlEncoder().encodeToString(bytes);

        CookieUtil.addCookie(
                response,
                COOKIE_NAME,
                value,
                domainFor(request),            // ✅ 로컬이면 null (호스트=localhost/127.0.0.1)
                true,                          // HttpOnly
                secureFor(request),            // ✅ http면 false, https면 true
                "Lax",                         // OAuth 콜백(GET)에는 Lax로 충분
                MAX_AGE_SEC
        );
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        var req = loadAuthorizationRequest(request);
        CookieUtil.addCookie(
                response,
                COOKIE_NAME,
                "",
                domainFor(request),
                true,
                secureFor(request),
                "Lax",
                0
        );
        return req;
    }

    private String domainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host == null) return null;
        // 운영 도메인/서브도메인에서만 Domain 지정
        if (prodDomain != null &&
                (host.equals(prodDomain) || host.endsWith("." + prodDomain))) {
            return prodDomain;
        }
        // 로컬/개발(예: localhost, 127.0.0.1, devbox) 에서는 null
        return null;
    }

    private boolean secureFor(HttpServletRequest req) {
        // HTTPS일 때만 Secure=true, HTTP(로컬)에서는 false → 쿠키 저장/전송 보장
        return req.isSecure();
    }
}
