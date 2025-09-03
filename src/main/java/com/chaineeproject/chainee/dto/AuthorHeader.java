package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "공고 작성자 헤더 정보")
public record AuthorHeader(
        Long id,
        String name,
        String profileImageUrl,
        List<String> positions,
        String from,
        String in,
        String website,
        @Schema(description = "팔로워 수") long followerCount,
        @Schema(description = "팔로잉 수") long followingCount
) {}