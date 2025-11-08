// src/main/java/com/chaineeproject/chainee/did/DidController.java
package com.chaineeproject.chainee.did;

import com.chaineeproject.chainee.did.dto.DidVerifyRequest;
import com.chaineeproject.chainee.did.dto.DidVerifyResponse;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/did")
@RequiredArgsConstructor
@Tag(name = "DID", description = "프론트 검증 결과를 반영해 didVerified 상태와 did 주소를 업데이트")
@SecurityRequirement(name = "bearerAuth")
public class DidController {

    private final DidService didService;
    private final UserRepository userRepository;

    private User me(Jwt jwt) {
        Long id = Long.valueOf(jwt.getSubject());
        return userRepository.findById(id).orElseThrow();
    }

    @Operation(
            summary = "DID 검증 상태 + 주소 반영",
            description = """
                    프론트에서 DID 검증을 끝낸 뒤 결과를 서버에 반영합니다.
                    - verified=true이면 DID 주소(did:로 시작)를 함께 보내 저장합니다.
                    - verified=false이면 검증 상태를 해제하고 저장된 DID를 제거합니다(정책).
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DidVerifyRequest.class),
                            examples = {
                                    @ExampleObject(name="검증 완료 반영", value = """
                                            { "verified": true, "did": "did:key:z6Mkabc123..." }
                                            """),
                                    @ExampleObject(name="검증 해제 반영", value = """
                                            { "verified": false }
                                            """)
                            }
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "200", description = "반영 결과",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DidVerifyResponse.class),
                            examples = {
                                    @ExampleObject(name="완료 응답", value = """
                                            {
                                              "success": true,
                                              "didVerified": true,
                                              "did": "did:key:z6Mkabc123...",
                                              "didVerifiedAt": "2025-10-14T21:35:12.345"
                                            }
                                            """),
                                    @ExampleObject(name="해제 응답", value = """
                                            {
                                              "success": true,
                                              "didVerified": false,
                                              "did": null,
                                              "didVerifiedAt": null
                                            }
                                            """)
                            }
                    )
            )
    )
    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verify(@AuthenticationPrincipal Jwt jwt,
                                    @RequestBody @Valid DidVerifyRequest req) {
        try {
            var updated = didService.updateDidVerified(me(jwt), req.verified(), req.did());
            return ResponseEntity.ok(new DidVerifyResponse(
                    true,
                    updated.isDidVerified(),
                    updated.getDid(),
                    updated.getDidVerifiedAt() != null ? updated.getDidVerifiedAt().toString() : null
            ));
        } catch (IllegalArgumentException e) {
            // 형식 오류 등 클라이언트 책임 에러는 400으로 반환
            return ResponseEntity.badRequest().body(new DidVerifyResponse(
                    false,
                    false,
                    null,
                    null
            ));
        }
    }
}
