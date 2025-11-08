// src/main/java/com/chaineeproject/chainee/dto/AuthExchangeRequest.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "AuthExchangeRequest", description = "loginCode 교환 요청")
public record AuthExchangeRequest(
        @Schema(description = "OAuth2 성공 후 fragment(#)로 전달된 1회용 코드", example = "40253d90a3e047d4a2d47f21be0646d1")
        @NotBlank
        String loginCode
) {}
