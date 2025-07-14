package com.chaineeproject.chainee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobPostResponse {
    private boolean success;
    private String messageCode;
    private DataWrapper data;

    @Data
    @AllArgsConstructor
    public static class DataWrapper {
        private String postId;
    }
}
