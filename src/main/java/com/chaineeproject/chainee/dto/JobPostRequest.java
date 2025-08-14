package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "구인 공고 등록 요청 DTO")
public record JobPostRequest(
        @Schema(description = "작성자 DID", example = "did:example:abc123")
        String authorDid,

        @Schema(description = "공고 제목", example = "백엔드 개발자 구합니다")
        String title,

        @Schema(description = "공고 설명", example = "Spring Boot 프로젝트 개발자 모집")
        String description,

        @Schema(description = "지급 금액", example = "1500000")
        BigDecimal payment,

        @Schema(description = "요구 기술 목록", example = "[\"Java\", \"Spring Boot\", \"JPA\"]")
        List<String> requiredSkills,

        @Schema(description = "프로젝트 기간 (예: 6개월은 6m)", example = "6m")
        String duration,

        @Schema(description = "마감일", example = "2025-09-30")
        LocalDate deadline
) {}
