package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "구인 공고 DTO")
public record JobPostDto(
        Long id,
        String title,
        String description,
        BigDecimal payment,
        String requiredSkills,
        LocalDate deadline,
        long applicantCount
) {}
