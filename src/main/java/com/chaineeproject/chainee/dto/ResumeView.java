package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "ResumeView", description = "이력서 조회 DTO")
public record ResumeView(
        Long id,
        String title,
        String name,
        String introduction,
        String desiredPosition,
        List<String> skills,
        String careerLevel,
        String portfolioUrl,
        LocalDateTime createdAt
) {}
