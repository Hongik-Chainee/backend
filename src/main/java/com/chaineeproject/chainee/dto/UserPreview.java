package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "팔로워/팔로잉 사용자 간단 정보")
public record UserPreview(
        Long id,
        String name,
        String profileImageUrl,
        String did,
        @Schema(description = "사용자 포지션 목록", example = "[\"Frontend\", \"UI/UX\"]")
        List<String> positions
) {}
