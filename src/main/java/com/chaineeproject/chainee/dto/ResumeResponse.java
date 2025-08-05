package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "이력서 등록 응답 DTO")
public class ResumeResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 코드", example = "RESUME_CREATED")
    private String messageCode;

    private DataWrapper data;

    public ResumeResponse(boolean success, String messageCode, Long resumeId) {
        this.success = success;
        this.messageCode = messageCode;
        this.data = new DataWrapper(resumeId);
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "이력서 ID 래퍼")
    public static class DataWrapper {
        @Schema(description = "생성된 이력서 ID", example = "1001")
        private Long resumeId;
    }
}
