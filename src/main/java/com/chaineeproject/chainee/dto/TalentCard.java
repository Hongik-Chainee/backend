package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "탤런트(사용자) 카드 DTO")
public record TalentCard(
        Long id,
        String name,
        String location,          // inLocation
        String profileImageUrl,
        List<String> positions,
        TalentProjectPreview mainProject,
        @Schema(description = "메인 프로젝트의 기술스택")
        List<String> requiredSkills,
        long followerCount,
        long followingCount,
        boolean isFollowing       // 요청자 기준
) {}
