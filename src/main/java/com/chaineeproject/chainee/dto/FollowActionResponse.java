package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팔로우/언팔로우 액션 응답")
public record FollowActionResponse(
        boolean success,
        String code
) {}
