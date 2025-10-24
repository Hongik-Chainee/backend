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
@Tag(name = "DID", description = "프론트 검증 결과를 반영해 didVerified 상태를 업데이트")
@SecurityRequirement(name = "bearerAuth")
public class DidController {

    private final DidService didService;
    private final UserRepository userRepository;

    private User me(Jwt jwt) {
        Long id = Long.valueOf(jwt.getSubject());
        return userRepository.findById(id).orElseThrow();
    }

    @Operation(
            summary = "DID 검증 상태 반영",
            description = "프론트에서 DID 검증을 끝낸 뒤 결과를 서버에 반영합니다. (문자열 DID는 저장하지 않음)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DidVerifyRequest.class),
                            examples = {
                                    @ExampleObject(name="검증 완료로 반영", value = "{ \"verified\": true }"),
                                    @ExampleObject(name="검증 해제로 반영", value = "{ \"verified\": false }")
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
                                              "didVerifiedAt": "2025-10-14T21:35:12.345"
                                            }
                                            """),
                                    @ExampleObject(name="해제 응답", value = """
                                            {
                                              "success": true,
                                              "didVerified": false,
                                              "didVerifiedAt": null
                                            }
                                            """)
                            }
                    )
            )
    )
    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DidVerifyResponse> verify(@AuthenticationPrincipal Jwt jwt,
                                                    @RequestBody @Valid DidVerifyRequest req) {
        var updated = didService.updateDidVerified(me(jwt), req.verified());
        return ResponseEntity.ok(new DidVerifyResponse(
                true,
                updated.isDidVerified(),
                updated.getDidVerifiedAt() != null ? updated.getDidVerifiedAt().toString() : null
        ));
    }
}
