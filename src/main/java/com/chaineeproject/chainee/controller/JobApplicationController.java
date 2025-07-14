package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.JobApplicationRequest;
import com.chaineeproject.chainee.dto.JobApplicationResponse;
import com.chaineeproject.chainee.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job/posts")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{postId}/apply")
    public ResponseEntity<JobApplicationResponse> applyToJob(
            @PathVariable Long postId,
            @RequestBody JobApplicationRequest request
    ) {
        jobApplicationService.applyToJob(postId, request);
        return ResponseEntity.ok(new JobApplicationResponse(true, "APPLICATION_SUBMITTED"));
    }
}
