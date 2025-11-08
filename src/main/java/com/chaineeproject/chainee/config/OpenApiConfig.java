package com.chaineeproject.chainee.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Chainee API", version = "v1"),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local 8080"),
                @Server(url = "https://chainee.store", description = "Prod")
        },
        // ✅ 전역 적용: 한 번 Authorize 하면 모든 API에 자동으로 Authorization 헤더 첨부
        security = { @SecurityRequirement(name = "bearerAuth") }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,   // ✅ HTTP bearer 스킴
        scheme = "bearer",                // ✅ swagger-ui가 자동으로 'Bearer ' 접두어를 붙임
        bearerFormat = "JWT",             // (표시용)
        description = "여기에 **토큰 값만** 입력하세요. 'Bearer '는 쓰지 마세요."
)
public class OpenApiConfig {}
