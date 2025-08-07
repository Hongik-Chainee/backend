package com.chaineeproject.chainee.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumeDto {
    private Long id;
    private String title;
    private String name;
    private String introduction;
    private String desiredPosition;
    private String skills;
    private String careerLevel;
    private String portfolioUrl;
}
