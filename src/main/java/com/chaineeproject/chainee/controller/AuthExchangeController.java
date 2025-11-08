// src/main/java/com/chaineeproject/chainee/controller/AuthExchangeController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.auth.AuthTokens;
import com.chaineeproject.chainee.auth.LoginCodeStore;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthExchangeController {

    private final LoginCodeStore loginCodeStore;
    private final AuthService authService;
    private final JwtProperties jwtProps;

    @PostMapping("/exchange")
    @Operation(summary = "loginCode 교환", description = "OAuth2 성공 후 fragment로 전달된 1회용 loginCode를 accessToken으로 교환합니다.")
    public ResponseEntity<?> exchange(@RequestBody Map<String, String> body,
                                      HttpServletRequest req,
                                      HttpServletResponse res) {
        String code = body.get("loginCode");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "MISSING_CODE"));
        }

        Long userId = loginCodeStore.consume(code);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "INVALID_OR_EXPIRED_CODE"));
        }

        // 유저 ID로 토큰 발급
        AuthTokens t = authService.issueForUserId(userId);

        // (옵션) refresh 쿠키 재세팅 — 도메인이 chainee.store일 때만 Domain 지정
        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        boolean secure  = Boolean.TRUE.equals(jwtProps.cookie().secure());
        String sameSite = jwtProps.cookie().sameSite();
        String cookieDomain = cookieDomainFor(req);
        CookieUtil.addCookie(
                res,
                jwtProps.refreshCookieName(),
                t.refreshToken(),
                cookieDomain,
                true,
                secure,
                sameSite,
                maxAge
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "accessToken", t.accessToken(),
                "accessExp", t.accessExpEpochSec(),
                "refreshExp", t.refreshExpEpochSec()
        ));
    }

    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null;
    }
}
