// src/main/java/com/chaineeproject/chainee/dto/AuthExchangeResponse.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthExchangeResponse", description = "loginCode 교환 성공 응답")
public record AuthExchangeResponse(
        @Schema(example = "true")
        boolean success,
        @Schema(description = "액세스 토큰(JWT)", example = "eyJraWQiOiJjaGFpbmVlLWtpZC0xIi...")
        String accessToken,
        @Schema(description = "액세스 토큰 만료(초, epoch)", example = "1762617794")
        Long accessExp,
        @Schema(description = "리프레시 토큰 만료(초, epoch)", example = "1763826494")
        Long refreshExp
) {}
