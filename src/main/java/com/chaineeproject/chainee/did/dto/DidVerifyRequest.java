package com.chaineeproject.chainee.did.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DidVerifyRequest", description = "DID 검증 요청 바디")
public record DidVerifyRequest(
        @Schema(description = "연동할 DID 식별자", example = "did:sol:6h5Z5o3P8TQfYd1wL2S3R8mV9PqKj8w2uB3F")
        String did,

        @Schema(description = "지갑 주소 (Base58 PublicKey, 32바이트)", example = "3ypq3qGxhw6sSpH3zv3C8q3vG6iZ3vR3FJ7mJYwRkQ6o")
        String address,

        @Schema(description = "nonce 원문에 대한 서명 (Base58, 64바이트)", example = "5dVjE3gD4mUXo8wPswj7H7w8f8n3zq3b2b...WmLcp")
        String signatureBase58
) {}
