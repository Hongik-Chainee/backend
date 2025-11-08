// src/main/java/com/chaineeproject/chainee/dto/ApplicationAcceptResponse.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApplicationAcceptResponse(
        @Schema(description = "처리 성공 여부", example = "true")
        boolean success,
        @Schema(description = "지원 상태", example = "accepted")
        String status,
        @Schema(description = "구직자 DID 주소", example = "did:key:z6Mkv...")
        String applicantDid,
        @Schema(description = "공고 지급 금액", example = "1500000")
        BigDecimal payment,
        @Schema(description = "공고 마감일(채용 마감)", example = "2025-09-30")
        LocalDate deadline
) {}
