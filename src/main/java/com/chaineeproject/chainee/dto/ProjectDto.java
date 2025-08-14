package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "프로젝트 DTO")
public record ProjectDto(
        Long id,
        String title,
        String nftTokenUri,
        LocalDateTime completedAt,
        Boolean isPublic
) {}
