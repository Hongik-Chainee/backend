package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.ResumeRequest;
import com.chaineeproject.chainee.dto.ResumeResponse;
import com.chaineeproject.chainee.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(@RequestBody ResumeRequest request) {
        Long resumeId = resumeService.createResume(request);
        return ResponseEntity.ok(new ResumeResponse(true, "RESUME_CREATED", resumeId));
    }
}