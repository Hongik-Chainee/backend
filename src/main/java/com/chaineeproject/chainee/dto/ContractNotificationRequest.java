// src/main/java/com/chaineeproject/chainee/dto/ContractNotificationRequest.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계약서 전자서명 요청 알림 생성 요청")
public record ContractNotificationRequest(

        @Schema(
                description = "프론트에서 생성한 트랜잭션 문자열 그대로 (JSON이든 base64든 어떤 형식이든 상관 없음)",
                example = "{ \"chain\": \"solana\", \"network\": \"devnet\", \"tx\": \"...\" }"
        )
        String transaction
) {}
