// src/main/java/com/chaineeproject/chainee/dto/ContractNotificationResponse.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계약서 전자서명 요청 알림 생성 응답")
public record ContractNotificationResponse(

        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "메시지 코드", example = "CONTRACT_NOTIFICATION_SENT")
        String messageCode,

        @Schema(description = "생성된 알림 ID", example = "101")
        Long notificationId
) {}
