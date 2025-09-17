// src/main/java/com/chaineeproject/chainee/controller/AuthController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProps;

    public AuthController(AuthService authService, JwtProperties jwtProps) {
        this.authService = authService;
        this.jwtProps = jwtProps;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest req, HttpServletResponse res) {
        Cookie c = CookieUtil.getCookie(req, jwtProps.refreshCookieName());
        if (c == null || c.getValue().isBlank())
            return ResponseEntity.status(401).body(Map.of("success", false, "message","NO_REFRESH"));

        var t = authService.rotate(c.getValue());
        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        CookieUtil.addCookie(
                res, jwtProps.refreshCookieName(), t.refreshToken(),
                cookieDomainFor(req),
                true, Boolean.TRUE.equals(jwtProps.cookie().secure()),
                jwtProps.cookie().sameSite(), maxAge
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "accessToken", t.accessToken(),
                "accessExp", t.accessExpEpochSec(),
                "refreshExp", t.refreshExpEpochSec()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req, HttpServletResponse res) {
        Cookie c = CookieUtil.getCookie(req, jwtProps.refreshCookieName());
        if (c != null && !c.getValue().isBlank()) authService.logout(c.getValue());
        CookieUtil.addCookie(
                res, jwtProps.refreshCookieName(), "",
                cookieDomainFor(req),
                true, Boolean.TRUE.equals(jwtProps.cookie().secure()),
                jwtProps.cookie().sameSite(), 0
        );
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        var uid = ((Number) jwt.getClaim("uid")).longValue();
        var email = (String) jwt.getClaim("email");
        return ResponseEntity.ok(Map.of("uid", uid, "email", email, "exp", jwt.getExpiresAt()));
    }

    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null; // 그 외(서브도메인/로컬)에는 HostOnly
    }
}
