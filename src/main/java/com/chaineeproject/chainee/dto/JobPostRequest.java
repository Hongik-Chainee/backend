package com.chaineeproject.chainee.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class JobPostRequest {

    private String authorDid;
    private String title;
    private String description;
    private BigDecimal payment;
    private List<String> requiredSkills;
    private String duration;  // "6m", "1y"
    private LocalDate deadline;
}
