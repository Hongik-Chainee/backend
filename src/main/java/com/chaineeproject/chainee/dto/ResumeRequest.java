package com.chaineeproject.chainee.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResumeRequest {
    private String applicantDid;
    private String title;
    private String name;
    private String introduction;
    private String desiredPosition;
    private List<String> skills;
    private String careerLevel;
    private String portfolioUrl;
}