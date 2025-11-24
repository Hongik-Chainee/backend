// src/main/java/com/chaineeproject/chainee/dto/ContractNotificationRequest.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계약서 전자서명(요청/완료) 알림 생성 요청")
public record ContractNotificationRequest(

        @Schema(
                description = "프론트에서 생성한 트랜잭션 payload 문자열 (JSON이든 base64든 어떤 형식이든 상관 없음)",
                example = "{ \"chain\": \"solana\", \"network\": \"devnet\", \"tx\": \"signed-tx-base64\" }"
        )
        String transaction,

        @Schema(
                description = "알림 클릭 시 이동할 계약서 페이지 엔드포인트(도메인 제외). 예: '/contracts/{id}/sign'",
                example = "/contracts/123/sign"
        )
        String linkUrl
) {}
