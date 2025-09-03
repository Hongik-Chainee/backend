// src/main/java/com/chaineeproject/chainee/controller/JobPostQueryController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.PagedJobPostsResponse;
import com.chaineeproject.chainee.service.JobPostQueryService;
import com.chaineeproject.chainee.service.JobPostQueryService.SortBy;
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
@Tag(name = "Job Post Query", description = "구인 공고 목록/검색/상세 API")
public class JobPostQueryController {

    private final JobPostQueryService jobPostQueryService;

    @GetMapping
    @Operation(
            summary = "구인 공고 목록",
            description = """
                전체 공고를 페이지네이션으로 반환합니다.
                - sort: RECENT / DEADLINE_ASC / DEADLINE_DESC / PAYMENT_ASC / PAYMENT_DESC / APPLICANTS_DESC
                - 기본값은 RECENT(createdAt DESC)
                """,
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
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
                                    "positions": ["HR","Recruiter"],
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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "RECENT") SortBy sort
    ) {
        return ResponseEntity.ok(jobPostQueryService.list(page, size, sort));
    }

    @GetMapping("/search")
    @Operation(
            summary = "구인 공고 검색",
            description = """
                제목/설명/요구기술/작성자이름에서 부분 일치 검색합니다.
                - q: 검색어 (필수)
                - sort: RECENT / DEADLINE_ASC / DEADLINE_DESC / PAYMENT_ASC / PAYMENT_DESC / APPLICANTS_DESC
                """,
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagedJobPostsResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "q=spring / RECENT",
                                            value = """
                                            {
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "page": 0,
                                              "size": 10,
                                              "posts": [
                                                {
                                                  "id": 201,
                                                  "title": "Spring Boot 백엔드 개발자",
                                                  "description": "신규 백엔드 모듈 개발...",
                                                  "payment": 3000000,
                                                  "requiredSkills": ["Java","Spring Boot","JPA"],
                                                  "duration": "6m",
                                                  "deadline": "2025-11-01",
                                                  "createdAt": "2025-08-20T09:00:00",
                                                  "applicantCount": 5,
                                                  "author": {
                                                    "id": 9,
                                                    "name": "Acme Corp HR",
                                                    "profileImageUrl": "https://cdn.example.com/u/9.png",
                                                    "positions": ["HR"],
                                                    "from": "SNU",
                                                    "in": "Seoul, Republic of Korea",
                                                    "website": "https://acme.example.com",
                                                    "followerCount": 90,
                                                    "followingCount": 8
                                                  }
                                                }
                                              ]
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<PagedJobPostsResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "RECENT") SortBy sort
    ) {
        return ResponseEntity.ok(jobPostQueryService.search(q, page, size, sort));
    }
}
