package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.JobPostRequest;
import com.chaineeproject.chainee.dto.JobPostResponse;
import com.chaineeproject.chainee.entity.JobPost;
import com.chaineeproject.chainee.entity.User;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
            description = "작성자의 DID를 바탕으로 구인 공고를 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobPostRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "authorDid": "did:example:abc123",
                                  "title": "백엔드 개발자 구합니다",
                                  "description": "Spring Boot 프로젝트 개발자 모집",
                                  "payment": 1500000,
                                  "requiredSkills": ["Java","Spring Boot","JPA"],
                                  "duration": "6m",
                                  "deadline": "2025-09-30"
                                }
                            """)
                    )
            )
    )
    public ResponseEntity<JobPostResponse> createJobPost(@RequestBody JobPostRequest request) {
        try {
            // 1) 작성자 DID로 유저 조회 (record 접근자 사용)
            User author = userRepository.findByDid(request.authorDid())
                    .orElseThrow(() -> new IllegalArgumentException("작성자(DID)를 찾을 수 없습니다."));

            // 2) requiredSkills → JSON 문자열로 변환
            String skillsJson = objectMapper.writeValueAsString(request.requiredSkills());

            // 3) 엔티티 생성 및 저장
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

            // 4) 응답 반환 (편의 생성자 사용)
            return ResponseEntity.ok(new JobPostResponse(
                    true,
                    "JOB_POST_CREATED",
                    "post-" + post.getId()
            ));

        } catch (JsonProcessingException e) {
            log.error("requiredSkills 변환 실패", e);
            return ResponseEntity.internalServerError().body(new JobPostResponse(
                    false,
                    "SKILL_PARSE_ERROR",
                    (String) null
            ));
        } catch (Exception e) {
            log.error("구인 공고 등록 실패", e);
            return ResponseEntity.internalServerError().body(new JobPostResponse(
                    false,
                    "JOB_POST_CREATION_FAILED",
                    (String) null
            ));
        }
    }
}
