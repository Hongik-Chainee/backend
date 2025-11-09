// src/main/java/com/chaineeproject/chainee/dto/JobPostRequest.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "구인 공고 생성 요청")
public record JobPostRequest(
        @Schema(description = "제목", example = "백엔드 개발자 구합니다")
        @NotBlank String title,

        @Schema(description = "설명", example = "Spring Boot 기반 API 개발/운영, 코드리뷰 문화")
        @NotBlank String description,

        @Schema(description = "급여/보상(숫자)", example = "3500000")
        @NotNull BigDecimal payment,

        @Schema(description = "요구 기술", example = "[\"Java\",\"Spring Boot\",\"JPA\",\"MySQL\"]")
        List<String> requiredSkills,

        @Schema(description = "기간(표기용)", example = "6m")
        String duration,

        @Schema(description = "마감일", example = "2025-12-31")
        LocalDate deadline
) {}
