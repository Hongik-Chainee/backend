package com.chaineeproject.chainee.kyc.dto;

import ch.qos.logback.core.status.Status;
import io.swagger.v3.oas.annotations.media.Schema;

public record KycPhoneVerifyResDto(
        @Schema(description = "KYC 인증 성공 여부", example = "true")
        boolean kycVerified,

        @Schema(description = "다음 스텝(예: DID, RETRY 등)", example = "DID")
        String nextStep,

        @Schema(description = "액세스 토큰(JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "액세스 토큰 만료 EpochSec", example = "1759153617")
        Long accessExp,

        @Schema(description = "리프레시 토큰 만료 EpochSec", example = "1759758417")
        Long refreshExp
) {}
