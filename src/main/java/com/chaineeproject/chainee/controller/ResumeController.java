package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.ResumeRequest;
import com.chaineeproject.chainee.dto.ResumeResponse;
import com.chaineeproject.chainee.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "이력서 등록 API")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    @Operation(summary = "이력서 등록", description = "지원자의 정보를 기반으로 이력서를 생성합니다.")
    public ResponseEntity<ResumeResponse> createResume(@RequestBody ResumeRequest request) {
        Long resumeId = resumeService.createResume(request);
        return ResponseEntity.ok(new ResumeResponse(true, "RESUME_CREATED", resumeId));
    }
}