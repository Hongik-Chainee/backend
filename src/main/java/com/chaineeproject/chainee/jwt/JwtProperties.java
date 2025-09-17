package com.chaineeproject.chainee.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String issuer,
        String audience,
        Duration accessTtl,
        Duration refreshTtl,
        Duration clockSkew,
        String refreshCookieName,
        Cookie cookie
) {
    public record Cookie(Boolean secure, String sameSite) {}
}
