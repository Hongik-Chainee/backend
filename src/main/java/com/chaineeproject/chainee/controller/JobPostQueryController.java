package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.JobPostDetail;
import com.chaineeproject.chainee.dto.PagedJobPostsResponse;
import com.chaineeproject.chainee.service.JobPostQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job/posts")
@RequiredArgsConstructor
@Tag(name = "Job Post Query", description = "구인 공고 목록/상세 API")
public class JobPostQueryController {

    private final JobPostQueryService jobPostQueryService;

    @GetMapping
    @Operation(
            summary = "구인 공고 목록",
            description = "작성자 프로필 헤더(프사/이름/포지션/from/in/website/팔로워/팔로잉)와 함께 공고 목록을 페이지네이션으로 반환합니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedJobPostsResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalElements": 42,
                              "totalPages": 5,
                              "page": 0,
                              "size": 10,
                              "posts": [
                                {
                                  "id": 101,
                                  "title": "백엔드 개발자 구합니다",
                                  "description": "Spring Boot 프로젝트 개발자 모집",
                                  "payment": 1500000,
                                  "requiredSkills": ["Java","Spring Boot","JPA"],
                                  "duration": "6m",
                                  "deadline": "2025-09-30",
                                  "createdAt": "2025-08-14T10:00:00",
                                  "applicantCount": 3,
                                  "author": {
                                    "id": 7,
                                    "name": "Acme Corp HR",
                                    "profileImageUrl": "https://cdn.example.com/u/7.png",
                                    "positions": ["HR", "Recruiter"],
                                    "from": "Yonsei Univ",
                                    "in": "Seoul, Republic of Korea",
                                    "website": "https://acme.example.com",
                                    "followerCount": 128,
                                    "followingCount": 12
                                  }
                                }
                              ]
                            }
                            """)
                    )
            )
    )
    public ResponseEntity<PagedJobPostsResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jobPostQueryService.list(page, size));
    }

    @GetMapping("/{postId}")
    @Operation(
            summary = "구인 공고 상세",
            description = "작성자 프로필 헤더(프사/이름/포지션/from/in/website/팔로워/팔로잉)를 포함한 공고 상세를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JobPostDetail.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "id": 101,
                                      "title": "백엔드 개발자 구합니다",
                                      "description": "Spring Boot 기반 신규 백엔드 모듈 개발 및 운영...",
                                      "payment": 1500000,
                                      "requiredSkills": ["Java","Spring Boot","JPA"],
                                      "duration": "6m",
                                      "deadline": "2025-09-30",
                                      "createdAt": "2025-08-14T10:00:00",
                                      "applicantCount": 3,
                                      "author": {
                                        "id": 7,
                                        "name": "Acme Corp HR",
                                        "profileImageUrl": "https://cdn.example.com/u/7.png",
                                        "positions": ["HR", "Recruiter"],
                                        "from": "Yonsei Univ",
                                        "in": "Seoul, Republic of Korea",
                                        "website": "https://acme.example.com",
                                        "followerCount": 128,
                                        "followingCount": 12
                                      }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공고")
            }
    )
    public ResponseEntity<JobPostDetail> detail(@PathVariable Long postId) {
        return ResponseEntity.ok(jobPostQueryService.detail(postId));
    }
}
