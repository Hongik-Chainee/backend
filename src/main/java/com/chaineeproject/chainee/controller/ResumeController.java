package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.*;
import com.chaineeproject.chainee.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "이력서 생성/조회/수정 API")
public class ResumeController {

    private final ResumeService resumeService;

    // ===== 생성 =====
    @PostMapping
    @Operation(
            summary = "이력서 생성",
            description = "지원자의 정보를 기반으로 이력서를 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResumeCreateRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "title": "프론트엔드 개발자 이력서",
                                  "name": "홍길동",
                                  "introduction": "React와 Next.js 기반 프로젝트를 다수 진행했습니다.",
                                  "desiredPosition": "프론트엔드 개발자",
                                  "skills": ["React", "TypeScript", "TailwindCSS"],
                                  "careerLevel": "3-5년",
                                  "portfolioUrl": "https://portfolio.example.com"
                                }
                            """)
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "200", description = "생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResumeResponse.class)))
    )
    public ResponseEntity<ResumeResponse> createResume(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ResumeCreateRequest request
    ) {
        Long currentUserId = com.chaineeproject.chainee.security.SecurityUtils.uidOrNull(jwt);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        Long resumeId = resumeService.createResumeFor(currentUserId, request);
        return ResponseEntity.ok(new ResumeResponse(true, "RESUME_CREATED", resumeId));
    }

    // ===== 조회(본인 목록) =====
    @GetMapping("/me")
    @Operation(
            summary = "내 이력서 목록",
            description = "로그인한 사용자의 이력서 목록을 반환합니다.",
            responses = @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResumeView.class)),
                            examples = @ExampleObject(value = """
                                [
                                  {
                                    "id": 10,
                                    "title": "프론트엔드 개발자 이력서",
                                    "name": "홍길동",
                                    "desiredPosition": "프론트엔드 개발자",
                                    "skills": ["React", "TypeScript", "TailwindCSS"],
                                    "careerLevel": "3-5년",
                                    "portfolioUrl": "https://portfolio.example.com"
                                  }
                                ]
                            """)))
    )
    public ResponseEntity<List<ResumeView>> getMyResumes(@AuthenticationPrincipal Jwt jwt) {
        Long currentUserId = com.chaineeproject.chainee.security.SecurityUtils.uidOrNull(jwt);
        if (currentUserId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(resumeService.getMyResumes(currentUserId));
    }

    // ===== 조회(본인 단건) =====
    @GetMapping("/{resumeId}")
    @Operation(
            summary = "내 이력서 상세",
            description = "로그인한 사용자의 특정 이력서를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResumeView.class))),
                    @ApiResponse(responseCode = "403", description = "본인 소유 아님")
            }
    )
    public ResponseEntity<ResumeView> getMyResume(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long resumeId
    ) {
        Long currentUserId = com.chaineeproject.chainee.security.SecurityUtils.uidOrNull(jwt);
        if (currentUserId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(resumeService.getMyResume(currentUserId, resumeId));
    }

    // ===== 수정(본인) =====
    @PutMapping("/{resumeId}")
    @Operation(
            summary = "내 이력서 수정",
            description = "로그인한 사용자의 특정 이력서를 부분 업데이트합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResumeUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "title": "프론트엔드 개발자 이력서 v2",
                                  "introduction": "사용자 경험에 집중하는 FE 개발자",
                                  "skills": ["React", "Next.js", "TypeScript"]
                                }
                            """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정된 이력서 반환",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResumeView.class))),
                    @ApiResponse(responseCode = "403", description = "본인 소유 아님")
            }
    )
    public ResponseEntity<ResumeView> updateMyResume(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long resumeId,
            @RequestBody ResumeUpdateRequest req
    ) {
        Long currentUserId = com.chaineeproject.chainee.security.SecurityUtils.uidOrNull(jwt);
        if (currentUserId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(resumeService.updateMyResume(currentUserId, resumeId, req));
    }
}
