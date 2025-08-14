package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이력서 등록 응답 DTO")
public record ResumeResponse(
        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 코드", example = "RESUME_CREATED")
        String messageCode,

        Data data
) {
    /** 편의 생성자: resumeId만 받아 data를 구성 */
    public ResumeResponse(boolean success, String messageCode, Long resumeId) {
        this(success, messageCode, new Data(resumeId));
    }

    @Schema(description = "이력서 ID 래퍼")
    public static record Data(
            @Schema(description = "생성된 이력서 ID", example = "1001")
            Long resumeId
    ) {}
}
