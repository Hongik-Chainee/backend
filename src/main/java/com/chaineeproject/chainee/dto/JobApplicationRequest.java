package com.chaineeproject.chainee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobApplicationRequest {
    private String applicantDid;
    private Long resumeId;
    private String proposal;
}
