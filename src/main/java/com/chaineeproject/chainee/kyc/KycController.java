package com.chaineeproject.chainee.kyc;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import com.chaineeproject.chainee.kyc.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "휴대폰 본인인증 (요청/검증)")
@SecurityRequirement(name = "bearerAuth") // JWT 필요
public class KycController {

    private final KycPhoneService kycPhoneService;
    private final UserRepository userRepo;

    private final AuthService authService;
    private final JwtProperties jwtProps;

    private User me(Jwt jwt) {
        Long id = Long.valueOf(jwt.getSubject());
        return userRepo.findById(id).orElseThrow();
    }

    @Operation(
            summary = "휴대폰 인증요청",
            description = "입력한 휴대폰 번호로 6자리 코드를 전송합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = KycPhoneRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "예시",
                                            value = """
                                                    {
                                                      "phone": "01012345678"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "요청 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = KycPhoneRequestResDto.class),
                                    examples = @ExampleObject(
                                            name = "성공 응답",
                                            value = """
                                                    {
                                                      "requestId": "8e5d1a0e-2c39-4a39-b73f-18d8d5b2c9f1",
                                                      "sent": true
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping(value = "/phone/request", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public KycPhoneRequestResDto request(@AuthenticationPrincipal Jwt jwt,
                                         @RequestBody @Valid KycPhoneRequestDto body) {
        String requestId = kycPhoneService.requestCode(me(jwt), body.phone(), "Chainee");
        return new KycPhoneRequestResDto(requestId, true);
    }

    @Operation(
            summary = "휴대폰 인증검증",
            description = "전송된 6자리 코드를 검증하고, 성공 시 access/refresh를 재발급합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = KycPhoneVerifyDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "성공 예시",
                                            value = """
                                                    {
                                                      "requestId": "8e5d1a0e-2c39-4a39-b73f-18d8d5b2c9f1",
                                                      "code": "123456"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "검증 성공/실패",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = KycPhoneVerifyResDto.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "성공 응답",
                                                    value = """
                                                            {
                                                              "kycVerified": true,
                                                              "nextStep": "DID",
                                                              "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                                                              "accessExp": 1759153617,
                                                              "refreshExp": 1759758417
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "실패 응답",
                                                    value = """
                                                            {
                                                              "kycVerified": false,
                                                              "nextStep": "RETRY",
                                                              "accessToken": null,
                                                              "accessExp": null,
                                                              "refreshExp": null
                                                            }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    @PostMapping(value = "/phone/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KycPhoneVerifyResDto> verify(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestBody @Valid KycPhoneVerifyDto body,
                                                       HttpServletResponse res) {
        User user = me(jwt);
        boolean ok = kycPhoneService.verifyCode(user, body.requestId(), body.code());
        if (!ok) {
            return ResponseEntity.ok(
                    new KycPhoneVerifyResDto(false, "RETRY", null, null, null)
            );
        }

        var tokens = authService.issueFor(user);

        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        CookieUtil.addCookie(
                res,
                jwtProps.refreshCookieName(),
                tokens.refreshToken(),
                "chainee.store",                                    // 로컬 개발 시 null 권장
                true,                                               // HttpOnly
                Boolean.TRUE.equals(jwtProps.cookie().secure()),    // Secure
                jwtProps.cookie().sameSite(),                       // Lax/None
                maxAge
        );

        return ResponseEntity.ok(
                new KycPhoneVerifyResDto(true, "DID", tokens.accessToken(), tokens.accessExpEpochSec(), tokens.refreshExpEpochSec())
        );
    }
}
