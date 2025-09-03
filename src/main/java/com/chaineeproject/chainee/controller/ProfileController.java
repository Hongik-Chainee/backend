package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.MainProjectUpdateRequest;
import com.chaineeproject.chainee.dto.ProfileResponse;
import com.chaineeproject.chainee.dto.ProfileUpdateRequest;
import com.chaineeproject.chainee.dto.ProjectVisibilityUpdateRequest;
import com.chaineeproject.chainee.service.ProfileService;
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
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 조회/수정 API")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(
            summary = "사용자 프로필 조회",
            description = """
            해당 userId의 프로필 정보를 반환합니다.
            - ✅ 본인이면 모든 내 프로젝트가 보입니다.
            - ❌ 타인이면 공개 프로젝트만 보입니다.
            - 대표 프로젝트는 지정값이 우선, 없거나(또는 타인에게 비공개이면) 최신(완료일→생성일) 프로젝트로 자동 선정됩니다.
            - followerCount / followingCount / isFollowing(요청자 기준) 포함
            - 각 jobPosts 항목에는 applicantCount(지원자 수) 포함
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "정상 응답",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Owner view",
                                                    value = """
                                                    {
                                                      "id": 1,
                                                      "email": "me@example.com",
                                                      "name": "Yoonseo Lee",
                                                      "profileImageUrl": "https://cdn.example.com/u/1.png",
                                                      "positions": ["Designer", "Illustrator"],
                                                      "from": "Hongik Univ",
                                                      "in": "Seoul, Republic of Korea",
                                                      "website": "https://www.yoonseolee.com",
                                                      "introductionHeadline": "Branding designer",
                                                      "introductionContent": "Hi! I'm a branding designer, just graduated.",
                                                      "introductionPhotoUrl": "https://cdn.example.com/u/1-cover.png",
                                                      "mainProject": { "id": 11, "title": "Portfolio 2025", "nftTokenUri": "ipfs://...", "completedAt": "2025-07-01T12:00:00", "isPublic": true },
                                                      "myProjects": [
                                                        { "id": 11, "title": "Portfolio 2025", "nftTokenUri": "ipfs://...", "completedAt": "2025-07-01T12:00:00", "isPublic": true },
                                                        { "id": 12, "title": "Client A", "nftTokenUri": "ipfs://...", "completedAt": "2025-05-20T09:00:00", "isPublic": false }
                                                      ],
                                                      "jobPosts": [
                                                        {
                                                          "id": 101,
                                                          "title": "백엔드 개발자 구합니다",
                                                          "description": "Spring Boot 프로젝트 개발자 모집",
                                                          "payment": 1500000,
                                                          "requiredSkills": "[\\"Java\\", \\"Spring Boot\\", \\"JPA\\"]",
                                                          "deadline": "2025-09-30",
                                                          "applicantCount": 3
                                                        }
                                                      ],
                                                      "resumes": [],
                                                      "followerCount": 128,
                                                      "followingCount": 64,
                                                      "isFollowing": false
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Public view",
                                                    value = """
                                                    {
                                                      "id": 1,
                                                      "email": "user@example.com",
                                                      "name": "Yoonseo Lee",
                                                      "profileImageUrl": "https://cdn.example.com/u/1.png",
                                                      "positions": ["Designer", "Illustrator"],
                                                      "from": "Hongik Univ",
                                                      "in": "Seoul, Republic of Korea",
                                                      "website": "https://www.yoonseolee.com",
                                                      "introductionHeadline": "Branding designer",
                                                      "introductionContent": "Hi! I'm a branding designer, just graduated.",
                                                      "introductionPhotoUrl": "https://cdn.example.com/u/1-cover.png",
                                                      "mainProject": { "id": 11, "title": "Portfolio 2025", "nftTokenUri": "ipfs://...", "completedAt": "2025-07-01T12:00:00", "isPublic": true },
                                                      "myProjects": [
                                                        { "id": 11, "title": "Portfolio 2025", "nftTokenUri": "ipfs://...", "completedAt": "2025-07-01T12:00:00", "isPublic": true }
                                                      ],
                                                      "jobPosts": [
                                                        {
                                                          "id": 101,
                                                          "title": "백엔드 개발자 구합니다",
                                                          "description": "Spring Boot 프로젝트 개발자 모집",
                                                          "payment": 1500000,
                                                          "requiredSkills": "[\\"Java\\", \\"Spring Boot\\", \\"JPA\\"]",
                                                          "deadline": "2025-09-30",
                                                          "applicantCount": 3
                                                        }
                                                      ],
                                                      "resumes": [],
                                                      "followerCount": 128,
                                                      "followingCount": 64,
                                                      "isFollowing": true
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 사용자",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                            {"status":404,"message":"사용자를 찾을 수 없습니다."}
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal(expression = "id") Long requesterUserId,
            @PathVariable Long userId
    ) {
        ProfileResponse response = profileService.getProfile(requesterUserId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "이름/이미지/포지션/소개 등 프로필 정보를 수정합니다(부분 업데이트).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileUpdateRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "name": "Yoonseo Lee",
                                      "profileImageUrl": "https://cdn.example.com/u/1.png",
                                      "positions": ["Designer", "Illustrator", "UI"],
                                      "from": "Hongik Univ",
                                      "inLocation": "Seoul, Republic of Korea",
                                      "website": "https://www.yoonseolee.com",
                                      "introductionHeadline": "Branding designer",
                                      "introductionContent": "Hi! I'm a branding designer, just graduated."
                                    }
                                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정된 프로필 반환",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))
                    )
            }
    )
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @RequestBody ProfileUpdateRequest req
    ) {
        ProfileResponse response = profileService.updateProfile(currentUserId, req);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "대표 프로젝트 설정/해제",
            description = "대표 프로젝트로 지정하거나(null이면 해제). 본인 소유 프로젝트만 지정 가능. 대표로 지정 시 자동 공개 처리됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MainProjectUpdateRequest.class),
                            examples = {
                                    @ExampleObject(name = "Set main",  value = "{\"mainProjectId\": 11}"),
                                    @ExampleObject(name = "Unset main", value = "{\"mainProjectId\": null}")
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정된 프로필 반환",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))
                    )
            }
    )
    @PatchMapping("/me/main-project")
    public ResponseEntity<ProfileResponse> updateMainProject(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @RequestBody MainProjectUpdateRequest req
    ) {
        ProfileResponse response = profileService.updateMainProject(currentUserId, req);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/projects/{projectId}/visibility")
    @Operation(
            summary = "프로젝트 공개/비공개 설정",
            description = """
                단일 프로젝트의 공개 여부를 변경합니다.
                - 본인 소유 프로젝트만 변경 가능
                - 대표 프로젝트는 반드시 공개여야 하며, 비공개로 설정 시 400 에러
                - 성공 시 최신 프로필(소유자 뷰) 반환
                """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectVisibilityUpdateRequest.class),
                            examples = {
                                    @ExampleObject(name = "공개로 전환", value = "{\"isPublic\": true}"),
                                    @ExampleObject(name = "비공개로 전환", value = "{\"isPublic\": false}")
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정된 프로필 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "대표 프로젝트 비공개 시도 / 잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = "{\"status\":400,\"message\":\"대표 프로젝트는 비공개로 설정할 수 없습니다.\"}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "본인 소유 아님",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = "{\"status\":403,\"message\":\"본인 프로젝트만 공개 여부를 변경할 수 있습니다.\"}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<ProfileResponse> updateProjectVisibility(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @PathVariable Long projectId,
            @RequestBody ProjectVisibilityUpdateRequest req
    ) {
        ProfileResponse response = profileService.updateProjectVisibility(currentUserId, projectId, req);
        return ResponseEntity.ok(response);
    }
}
