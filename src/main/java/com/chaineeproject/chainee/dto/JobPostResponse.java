package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구인 공고 등록 응답 DTO")
public record JobPostResponse(
        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 코드", example = "JOB_POST_CREATED")
        String messageCode,

        DataWrapper data
) {
    /** 편의 생성자: postId만 받아 data를 구성 */
    public JobPostResponse(boolean success, String messageCode, String postId) {
        this(success, messageCode, new DataWrapper(postId));
    }

    @Schema(description = "공고 ID 래퍼")
    public static record DataWrapper(
            @Schema(description = "등록된 공고의 ID", example = "post-12")
            String postId
    ) {}
}
