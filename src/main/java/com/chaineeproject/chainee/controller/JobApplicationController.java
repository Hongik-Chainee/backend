// src/main/java/com/chaineeproject/chainee/controller/JobApplicationController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.JobApplicationRequest;
import com.chaineeproject.chainee.dto.JobApplicationResponse;
import com.chaineeproject.chainee.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job/posts")
@Tag(name = "Job Application", description = "채용 공고 지원 관련 API")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{postId}/apply")
    @Operation(summary = "채용 공고 지원", description = "요청 사용자의 JWT uid를 지원자로 하여 지정한 공고에 지원합니다.")
    public ResponseEntity<JobApplicationResponse> applyToJob(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "지원할 채용 공고 ID", example = "1")
            @PathVariable Long postId,
            @Valid @RequestBody JobApplicationRequest request
    ) {
        Long applicantId = com.chaineeproject.chainee.security.SecurityUtils.uidOrNull(jwt);
        if (applicantId == null) return ResponseEntity.status(401).build();

        jobApplicationService.applyToJob(postId, applicantId, request.resumeId());
        return ResponseEntity.ok(new JobApplicationResponse(true, "APPLICATION_SUBMITTED"));
    }
}
