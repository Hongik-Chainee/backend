package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.ContractNotificationRequest;
import com.chaineeproject.chainee.dto.ContractNotificationResponse;
import com.chaineeproject.chainee.service.EmployerApplicationService;
import com.chaineeproject.chainee.security.SecurityUtils;
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
@RequestMapping("/api/job/applications")
@Tag(name = "Contract Notification", description = "계약 전자서명 관련 알림 API")
@SecurityRequirement(name = "bearerAuth")
public class ContractNotificationController {

    private final EmployerApplicationService employerApplicationService;

    private Long meId(Jwt jwt) {
        return SecurityUtils.uidOrNull(jwt);
    }

    /**
     * 구인자 → 구직자 : 계약서 전자 서명 요청 알림
     * 예: POST /api/job/applications/{applicationId}/contract-request
     * body: { "transaction": "{...raw tx...}" }
     */
    @PostMapping("/{applicationId}/contract-request")
    @Operation(
            summary = "계약서 전자 서명 요청 알림 (구인자 → 구직자)",
            description = "공고 작성자가 지원자에게 계약서 전자 서명을 요청하는 알림을 생성합니다."
    )
    public ResponseEntity<ContractNotificationResponse> sendContractRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long applicationId,
            @RequestBody ContractNotificationRequest request
    ) {
        Long employerId = meId(jwt);
        if (employerId == null) return ResponseEntity.status(401).build();

        var res = employerApplicationService.sendContractNotification(employerId, applicationId, request);
        return ResponseEntity.ok(res);
    }

    /**
     * 🔥 구직자 → 구인자 : 서명 완료 알림
     * 예: POST /api/job/applications/{applicationId}/contract-signed
     * body: { "transaction": "{...signed tx...}" }
     */
    @PostMapping("/{applicationId}/contract-signed")
    @Operation(
            summary = "계약서 전자 서명 완료 알림 (구직자 → 구인자)",
            description = "지원자가 계약서 전자 서명을 완료했음을 공고 작성자에게 알리는 알림을 생성합니다."
    )
    public ResponseEntity<ContractNotificationResponse> sendContractSigned(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long applicationId,
            @RequestBody ContractNotificationRequest request
    ) {
        Long applicantId = meId(jwt);
        if (applicantId == null) return ResponseEntity.status(401).build();

        var res = employerApplicationService.sendSignedContractNotification(applicantId, applicationId, request);
        return ResponseEntity.ok(res);
    }
}
