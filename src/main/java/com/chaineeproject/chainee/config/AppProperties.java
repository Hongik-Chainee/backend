package com.chaineeproject.chainee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Deprecated String frontendBaseUrl,   // 하위호환(단일)
        List<String> frontendBaseurls,        // 신규 멀티
        Oauth2Props oauth2
) {
    /** 멀티 설정이 있으면 우선, 없으면 단일 값으로 대체 */
    public List<String> effectiveBases() {
        if (frontendBaseurls != null && !frontendBaseurls.isEmpty()) return frontendBaseurls;
        return (frontendBaseUrl == null || frontendBaseUrl.isBlank())
                ? List.of()
                : List.of(frontendBaseUrl);
    }

    public record Oauth2Props(String callbackPath) {}
}
