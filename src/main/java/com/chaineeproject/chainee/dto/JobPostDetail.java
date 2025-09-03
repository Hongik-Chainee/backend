package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "구인 공고 상세")
public record JobPostDetail(
        Long id,
        String title,
        String description,
        BigDecimal payment,
        List<String> requiredSkills,
        String duration,
        LocalDate deadline,
        LocalDateTime createdAt,
        long applicantCount,
        AuthorHeader author
) {}
