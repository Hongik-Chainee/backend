// src/main/java/com/chaineeproject/chainee/controller/JobPostController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.JobPostRequest;
import com.chaineeproject.chainee.dto.JobPostResponse;
import com.chaineeproject.chainee.entity.JobPost;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.JobPostRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/job/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Post", description = "구인 공고 등록 API")
public class JobPostController {

    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ObjectMapper objectMapper;

    @PostMapping
    @Operation(
            summary = "구인 공고 등록",
            description = "요청 사용자의 JWT uid를 작성자로 하여 공고를 생성합니다. (authorId는 바디로 받지 않습니다)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobPostRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "title": "백엔드 개발자 구합니다",
                                  "description": "Spring Boot 기반 API 개발/운영, 코드리뷰 문화",
                                  "payment": 3500000,
                                  "requiredSkills": ["Java","Spring Boot","JPA","MySQL"],
                                  "duration": "6m",
                                  "deadline": "2025-12-31"
                                }
                            """)
                    )
            )
    )
    public ResponseEntity<JobPostResponse> createJobPost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
            JobPostRequest request
    ) {
        Long authorId = com.chaineeproject.chainee.security.SecurityUtils.uidOrNull(jwt);
        if (authorId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

            String skillsJson = objectMapper.writeValueAsString(
                    request.requiredSkills() == null ? List.of() : request.requiredSkills()
            );

            JobPost post = JobPost.builder()
                    .author(author)
                    .title(request.title())
                    .description(request.description())
                    .payment(request.payment())
                    .requiredSkills(skillsJson)
                    .duration(request.duration())
                    .deadline(request.deadline())
                    .createdAt(LocalDateTime.now())
                    .build();

            jobPostRepository.save(post);

            return ResponseEntity.ok(new JobPostResponse(true, "JOB_POST_CREATED", "post-" + post.getId()));

        } catch (JsonProcessingException e) {
            log.warn("requiredSkills 변환 실패", e);
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }
    }
}
