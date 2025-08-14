package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이력서 DTO")
public record ResumeDto(
        Long id,
        String title,
        String name,
        String introduction,
        String desiredPosition,
        String skills,
        String careerLevel,
        String portfolioUrl
) {}
