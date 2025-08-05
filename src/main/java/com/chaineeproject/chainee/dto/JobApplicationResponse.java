package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "채용 공고 지원 응답 DTO")
public class JobApplicationResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지 코드", example = "APPLICATION_SUBMITTED")
    private String messageCode;
}
