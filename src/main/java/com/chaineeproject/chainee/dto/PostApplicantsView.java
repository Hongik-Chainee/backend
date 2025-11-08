// src/main/java/com/chaineeproject/chainee/dto/PostApplicantsView.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PostApplicantsView(
        @Schema(example = "101") Long postId,
        @Schema(example = "백엔드 개발자 구합니다") String postTitle,
        @Schema(example = "7") long totalApplicants,
        List<ApplicantSummary> applicants
) {}
