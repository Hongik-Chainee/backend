package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.JobApplicationRequest;
import com.chaineeproject.chainee.dto.JobApplicationResponse;
import com.chaineeproject.chainee.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job/posts")
@Tag(name = "Job Application", description = "채용 공고 지원 관련 API")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{postId}/apply")
    @Operation(summary = "채용 공고 지원", description = "지정한 채용 공고 ID에 대해 지원서를 제출합니다.")
    public ResponseEntity<JobApplicationResponse> applyToJob(
            @Parameter(description = "지원할 채용 공고 ID", example = "1")
            @PathVariable Long postId,

            @RequestBody JobApplicationRequest request
    ) {
        jobApplicationService.applyToJob(postId, request);
        return ResponseEntity.ok(new JobApplicationResponse(true, "APPLICATION_SUBMITTED"));
    }
}
