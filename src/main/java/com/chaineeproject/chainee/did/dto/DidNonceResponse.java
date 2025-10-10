package com.chaineeproject.chainee.did.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DidNonceResponse", description = "DID 서명을 위한 nonce 응답")
public record DidNonceResponse(
        @Schema(description = "서명할 nonce (URL-safe Base64, padding 없음)",
                example = "6y7B8yB0W2bN8asbO5zQwH1P7C0s3-cUZG2p1KcB9ko")
        String nonce,

        @Schema(description = "nonce 만료까지 남은 초", example = "600")
        int expiresInSec
) {}
