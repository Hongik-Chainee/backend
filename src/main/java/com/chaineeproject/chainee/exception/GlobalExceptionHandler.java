package com.chaineeproject.chainee.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<?> handleApplicationException(ApplicationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("messageCode", ex.getErrorCode().name());
        body.put("error", ex.getErrorCode().getMessage());
        return ResponseEntity.ok(body); // 200 상태 유지
    }
}
