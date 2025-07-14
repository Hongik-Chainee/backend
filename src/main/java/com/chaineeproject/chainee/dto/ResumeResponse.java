package com.chaineeproject.chainee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResumeResponse {
    private boolean success;
    private String messageCode;
    private DataWrapper data;

    public ResumeResponse(boolean success, String messageCode, Long resumeId) {
        this.success = success;
        this.messageCode = messageCode;
        this.data = new DataWrapper(resumeId);
    }

    @Data
    @AllArgsConstructor
    public static class DataWrapper {
        private Long resumeId;
    }
}