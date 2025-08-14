package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채용 공고 지원 응답 DTO")
public record JobApplicationResponse(
        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 메시지 코드", example = "APPLICATION_SUBMITTED")
        String messageCode
) {}
