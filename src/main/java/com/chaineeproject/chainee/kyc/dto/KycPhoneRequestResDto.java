package com.chaineeproject.chainee.kyc.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record KycPhoneRequestResDto(
        @Schema(description = "요청 식별자(UUID)", example = "8e5d1a0e-2c39-4a39-b73f-18d8d5b2c9f1")
        String requestId,

        @Schema(description = "문자 전송 여부", example = "true")
        boolean sent
) {}
