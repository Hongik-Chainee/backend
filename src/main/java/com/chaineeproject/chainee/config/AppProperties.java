package com.chaineeproject.chainee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String frontendBaseUrl, Oauth2Props oauth2) {
    public record Oauth2Props(String callbackPath) {}
}
