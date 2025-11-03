package com.chaineeproject.chainee.kyc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record KycPhoneVerifyDto(
        @Schema(description = "요청 식별자(UUID)", example = "8e5d1a0e-2c39-4a39-b73f-18d8d5b2c9f1")
        @NotBlank
        String requestId,

        @Schema(description = "수신한 6자리 인증코드", example = "123456")
        @NotBlank
        String code,

        @Schema(description = "실명", example = "김여원")
        @NotBlank
        String name
) {}
