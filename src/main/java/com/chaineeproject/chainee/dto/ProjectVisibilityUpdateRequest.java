package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 공개/비공개 변경 요청")
public record ProjectVisibilityUpdateRequest(
        @Schema(description = "공개 여부", example = "true")
        Boolean isPublic
) {}