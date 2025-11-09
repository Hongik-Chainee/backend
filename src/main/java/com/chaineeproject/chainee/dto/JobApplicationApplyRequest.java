package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채용 공고 지원 요청 DTO(JWT uid 사용)")
public class JobApplicationApplyRequest {

    @Schema(description = "지원자가 선택한 이력서 ID", example = "42", required = true)
    private Long resumeId;
}
