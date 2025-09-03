package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "탤런트 카드에 노출되는 대표 프로젝트 프리뷰")
public record TalentProjectPreview(
        Long id,
        String title,
        String nftTokenUri,
        LocalDateTime completedAt
) {}