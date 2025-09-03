package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "계약 모달에 표시할 내 공고 요약")
public record ContractPostOption(
        @Schema(description = "공고 ID", example = "101") Long postId,
        @Schema(description = "공고 제목", example = "UI/UX 디자이너 모집") String title,
        @Schema(description = "급여", example = "5000000") BigDecimal payment,
        @Schema(description = "기간(예: 6m, 1y)", example = "6m") String duration
) {}