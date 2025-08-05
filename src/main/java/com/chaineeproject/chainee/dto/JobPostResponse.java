package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "구인 공고 등록 응답 DTO")
public class JobPostResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 코드", example = "JOB_POST_CREATED")
    private String messageCode;

    private DataWrapper data;

    @Data
    @AllArgsConstructor
    @Schema(description = "공고 ID 래퍼")
    public static class DataWrapper {
        @Schema(description = "등록된 공고의 ID", example = "post-12")
        private String postId;
    }
}
