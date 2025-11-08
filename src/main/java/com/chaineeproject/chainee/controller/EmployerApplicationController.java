// src/main/java/com/chaineeproject/chainee/controller/EmployerApplicationController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.ApplicationAcceptResponse;
import com.chaineeproject.chainee.dto.PostApplicantsView;
import com.chaineeproject.chainee.dto.ResumeView;
import com.chaineeproject.chainee.service.EmployerApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api") // 메서드별로 풀 패스 지정
@Tag(name = "Employer Application", description = "구인자용 지원/관리 API")
@SecurityRequirement(name = "bearerAuth")
public class EmployerApplicationController {

    private final EmployerApplicationService employerApplicationService;

    private Long meId(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }

    /** 지원 수락 */
    @PostMapping("/job/applications/{applicationId}/accept")
    @Operation(summary = "지원 수락",
            description = "구인자(작성자)가 특정 지원서를 수락합니다. 응답으로 구직자 DID, payment, deadline을 반환합니다.")
    public ResponseEntity<ApplicationAcceptResponse> accept(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long applicationId
    ) {
        var res = employerApplicationService.acceptApplication(applicationId, meId(jwt));
        return ResponseEntity.ok(res);
    }

    /** 공고 지원자 관리 목록 */
    @GetMapping("/job/posts/{postId}/applicants")
    @Operation(summary = "공고 지원자 관리 목록",
            description = "공고 제목, 총 지원자 수, 지원자 리스트(이름/지원 날짜/positions)를 반환합니다.")
    public ResponseEntity<PostApplicantsView> applicantsOfPost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(
                employerApplicationService.getApplicantsOfPost(meId(jwt), postId)
        );
    }

    /** 구인자 전용 이력서 열람 (applicationId로 접근) */
    @GetMapping("/job/applications/{applicationId}/resume")
    @Operation(summary = "지원서 기반 이력서 조회(구인자용)",
            description = "공고 작성자가 자신에게 접수된 지원서의 이력서를 열람합니다.")
    public ResponseEntity<ResumeView> getResumeForEmployer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                employerApplicationService.getResumeForEmployer(meId(jwt), applicationId)
        );
    }
}
