package com.chaineeproject.chainee.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class JobPostDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal payment;
    private String requiredSkills;
    private LocalDate deadline;
}
