package com.chaineeproject.chainee.did.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DidVerifyResponse(
        @Schema(description = "요청 처리 성공 여부", example = "true")
        boolean success,
        @Schema(description = "현재 DID 검증 상태", example = "true")
        boolean didVerified,
        @Schema(description = "검증 완료 시각(ISO-8601) - 해제 시 null", example = "2025-10-14T21:35:12.345")
        String didVerifiedAt
) {}
