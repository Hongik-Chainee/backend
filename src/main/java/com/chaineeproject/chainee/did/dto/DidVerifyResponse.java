package com.chaineeproject.chainee.did.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DidVerifyResponse", description = "DID 검증 성공 시 반환")
public record DidVerifyResponse(
        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "새 Access 토큰(JWT)", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Access 토큰 만료 EpochSec", example = "1760091753")
        Long accessExp,

        @Schema(description = "Refresh 토큰 만료 EpochSec", example = "1761300453")
        Long refreshExp
) {}
