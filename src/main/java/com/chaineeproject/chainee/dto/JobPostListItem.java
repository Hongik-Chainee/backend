package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "구인 공고 목록 아이템")
public record JobPostListItem(
        Long id,
        String title,
        String description,
        BigDecimal payment,
        List<String> requiredSkills,
        String duration,
        LocalDate deadline,
        LocalDateTime createdAt,
        long applicantCount,
        ProfileHeaderDto author
) {}
