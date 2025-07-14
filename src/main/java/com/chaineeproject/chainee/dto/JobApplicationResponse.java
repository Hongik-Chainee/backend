package com.chaineeproject.chainee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobApplicationResponse {
    private boolean success;
    private String messageCode;
}
