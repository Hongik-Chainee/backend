// src/main/java/com/chaineeproject/chainee/dto/JobApplicationRequest.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "채용 공고 지원 요청 DTO")
public record JobApplicationRequest(
        @Schema(description = "지원자가 제출할 이력서 ID", example = "42")
        @NotNull Long resumeId
) {}
