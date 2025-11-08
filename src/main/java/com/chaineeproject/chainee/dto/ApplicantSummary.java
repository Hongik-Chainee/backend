// src/main/java/com/chaineeproject/chainee/dto/ApplicantSummary.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record ApplicantSummary(
        @Schema(example = "555") Long applicationId,
        @Schema(example = "3") Long applicantId,
        @Schema(example = "홍길동") String name,
        @Schema(example = "2025-09-02T10:00:00") LocalDateTime appliedAt,
        @Schema(example = "[\"Backend\",\"DevOps\"]") List<String> positions
) {}
