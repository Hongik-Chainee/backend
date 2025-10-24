package com.chaineeproject.chainee.did.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DidVerifyRequest(
        @Schema(description = "프론트에서 검증한 DID 결과(true=검증완료로 반영, false=해제)", example = "true")
        @NotNull Boolean verified
) {}
