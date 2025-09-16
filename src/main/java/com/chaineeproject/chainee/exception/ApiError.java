package com.chaineeproject.chainee.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        boolean success,
        String messageCode,
        String error,
        int status,
        String path,
        Instant timestamp,
        Map<String, String> validation
) { }