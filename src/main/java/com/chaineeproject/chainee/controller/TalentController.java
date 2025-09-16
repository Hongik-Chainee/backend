package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.PagedTalentsResponse;
import com.chaineeproject.chainee.service.TalentQueryService;
import com.chaineeproject.chainee.service.TalentQueryService.SortBy;
import org.springframework.security.core.Authentication;
import static com.chaineeproject.chainee.security.SecurityUtils.extractUserIdOrNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/talents")
@RequiredArgsConstructor
@Tag(name = "Talent", description = "탤런트 조회/검색 API")
public class TalentController {

    private final TalentQueryService talentQueryService;

    @GetMapping
    @Operation(
            summary = "탤런트 목록",
            description = """
                탤런트 카드 리스트를 페이지네이션으로 반환합니다.
                - sort: RECENT / NAME / LOCATION / POPULAR
                - includeMainProject: true면 mainProject 미리보기 포함, false면 null
                """,
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "정상 응답",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagedTalentsResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "includeMainProject=true / RECENT",
                                            value = """
                                            {
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "page": 0,
                                              "size": 9,
                                              "talents": [
                                                {
                                                  "id": 23,
                                                  "name": "김여은",
                                                  "location": "Seoul, Republic of Korea",
                                                  "profileImageUrl": "https://cdn.example.com/u/23.png",
                                                  "positions": ["Designer","Illustrator"],
                                                  "mainProject": {
                                                    "id": 101,
                                                    "title": "Brand Portfolio 2025",
                                                    "nftTokenUri": "ipfs://bafybeigd.../portfolio.json",
                                                    "completedAt": "2025-07-01T12:00:00"
                                                  },
                                                  "requiredSkills": ["Figma","Illustrator","Branding"],
                                                  "followerCount": 87,
                                                  "followingCount": 14,
                                                  "isFollowing": false
                                                },
                                                {
                                                  "id": 24,
                                                  "name": "김여원",
                                                  "location": "Busan, Republic of Korea",
                                                  "profileImageUrl": "https://cdn.example.com/u/24.png",
                                                  "positions": ["Frontend","UI/UX"],
                                                  "mainProject": {
                                                    "id": 111,
                                                    "title": "Landing Redesign",
                                                    "nftTokenUri": "ipfs://bafkreihd.../landing.json",
                                                    "completedAt": "2025-06-12T09:30:00"
                                                  },
                                                  "requiredSkills": ["React","TypeScript","CSS"],
                                                  "followerCount": 12,
                                                  "followingCount": 33,
                                                  "isFollowing": true
                                                }
                                              ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "includeMainProject=false / NAME",
                                            value = """
                                            {
                                              "totalElements": 1,
                                              "totalPages": 1,
                                              "page": 0,
                                              "size": 9,
                                              "talents": [
                                                {
                                                  "id": 31,
                                                  "name": "Lee Anna",
                                                  "location": "Seoul, Republic of Korea",
                                                  "profileImageUrl": "https://cdn.example.com/u/31.png",
                                                  "positions": ["Backend","DevOps"],
                                                  "mainProject": null,
                                                  "requiredSkills": ["Spring Boot","JPA","MySQL"],
                                                  "followerCount": 40,
                                                  "followingCount": 5,
                                                  "isFollowing": false
                                                }
                                              ]
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<PagedTalentsResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "RECENT") SortBy sort,
            @RequestParam(defaultValue = "true") boolean includeMainProject
    ) {
        Long requesterUserId = extractUserIdOrNull(authentication);
        return ResponseEntity.ok(
                talentQueryService.list(requesterUserId, page, size, sort, includeMainProject)
        );
    }

    @GetMapping("/search")
    @Operation(
            summary = "탤런트 검색",
            description = """
            이름 / 포지션 / 지역 / 공개 프로젝트 기술스택에서 **부분 일치** 검색합니다.
            - 예: q="김여" → "김여은", "김여원" 모두 매칭
            - sort: RECENT / NAME / LOCATION / POPULAR
            - includeMainProject=false면 mainProject는 null
            """,
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "정상 응답",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagedTalentsResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "q=김여 / RECENT / includeMainProject=false",
                                            value = """
                                        {
                                          "totalElements": 2,
                                          "totalPages": 1,
                                          "page": 0,
                                          "size": 9,
                                          "talents": [
                                            {
                                              "id": 23,
                                              "name": "김여은",
                                              "location": "Seoul, Republic of Korea",
                                              "profileImageUrl": "https://cdn.example.com/u/23.png",
                                              "positions": ["Designer","Illustrator"],
                                              "mainProject": null,
                                              "requiredSkills": ["Figma","Illustrator","Branding"],
                                              "followerCount": 87,
                                              "followingCount": 14,
                                              "isFollowing": false
                                            },
                                            {
                                              "id": 24,
                                              "name": "김여원",
                                              "location": "Busan, Republic of Korea",
                                              "profileImageUrl": "https://cdn.example.com/u/24.png",
                                              "positions": ["Frontend","UI/UX"],
                                              "mainProject": null,
                                              "requiredSkills": ["React","TypeScript","CSS"],
                                              "followerCount": 12,
                                              "followingCount": 33,
                                              "isFollowing": true
                                            }
                                          ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "q=react / NAME / includeMainProject=true",
                                            value = """
                                        {
                                          "totalElements": 1,
                                          "totalPages": 1,
                                          "page": 0,
                                          "size": 9,
                                          "talents": [
                                            {
                                              "id": 55,
                                              "name": "Park Jihyun",
                                              "location": "Seoul, Republic of Korea",
                                              "profileImageUrl": "https://cdn.example.com/u/55.png",
                                              "positions": ["Frontend","UI"],
                                              "mainProject": {
                                                "id": 210,
                                                "title": "Admin Dashboard",
                                                "nftTokenUri": "ipfs://bafyrei.../admin.json",
                                                "completedAt": "2025-03-05T18:15:00"
                                              },
                                              "requiredSkills": ["React","TypeScript","TailwindCSS"],
                                              "followerCount": 5,
                                              "followingCount": 8,
                                              "isFollowing": false
                                            }
                                          ]
                                        }
                                        """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<PagedTalentsResponse> search(
            Authentication authentication,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "false") boolean includeMainProject,
            @RequestParam(defaultValue = "RECENT") SortBy sort
    ) {
        Long requesterUserId = extractUserIdOrNull(authentication);
        return ResponseEntity.ok(
                talentQueryService.search(requesterUserId, q, page, size, includeMainProject, sort)
        );
    }
}
