// src/main/java/com/chaineeproject/chainee/did/dto/DidVerifyRequest.java
package com.chaineeproject.chainee.did.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DidVerifyRequest(
        @Schema(description = "프론트에서 검증한 DID 결과(true=검증완료로 반영, false=해제)", example = "true")
        @NotNull Boolean verified,

        @Schema(description = "검증이 끝난 사용자의 DID 주소(did: 로 시작). verified=true일 때 권장/사실상 필수",
                example = "did:key:z6Mkw...abc")
        String did
) {}
