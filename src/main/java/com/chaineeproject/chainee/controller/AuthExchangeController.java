// src/main/java/com/chaineeproject/chainee/controller/AuthExchangeController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.auth.AuthTokens;
import com.chaineeproject.chainee.auth.LoginCodeStore;
import com.chaineeproject.chainee.dto.AuthExchangeRequest;
import com.chaineeproject.chainee.dto.AuthExchangeResponse;
import com.chaineeproject.chainee.dto.SimpleErrorResponse;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthExchangeController {

    private final LoginCodeStore loginCodeStore;
    private final AuthService authService;
    private final JwtProperties jwtProps;

    @PostMapping(value = "/exchange", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "loginCode 교환",
            description = "OAuth2 성공 후 fragment(#)로 전달된 1회용 loginCode를 accessToken으로 교환합니다.",
            // ⛔️ 전역 보안 해제: 이 엔드포인트는 인증 없이 호출 가능해야 함
            security = {},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthExchangeRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "샘플 요청",
                                            value = """
                                                    { "loginCode": "40253d90a3e047d4a2d47f21be0646d1" }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "교환 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthExchangeResponse.class),
                                    examples = @ExampleObject(
                                            name = "성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "accessToken": "eyJraWQiOiJjaGFpbmVlLWtpZC0xIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJjaGFpbmVlIiwic3ViIjoiNiIsInVpZCI6Niwia3ljIjpmYWxzZSwiaXNzIjoiaHR0cHM6Ly9jaGFpbmVlLnN0b3JlIiwiZXhwIjoxNzYyNjE3Nzk0LCJpYXQiOjE3NjI2MTY4OTQsImVtYWlsIjoidXNlckBleGFtcGxlLmNvbSIsImRpZCI6ZmFsc2V9.RrRQ56wjtD1m...",
                                                      "accessExp": 1762617794,
                                                      "refreshExp": 1763826494
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "코드 누락/만료/무효",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SimpleErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "코드 누락",
                                                    value = """
                                                            { "success": false, "message": "MISSING_CODE" }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "무효/만료 코드",
                                                    value = """
                                                            { "success": false, "message": "INVALID_OR_EXPIRED_CODE" }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<?> exchange(
            @Valid @RequestBody AuthExchangeRequest body,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        String code = body.loginCode();

        Long userId = loginCodeStore.consume(code);
        if (userId == null) {
            return ResponseEntity.badRequest().body(new SimpleErrorResponse(false, "INVALID_OR_EXPIRED_CODE"));
        }

        AuthTokens t = authService.issueForUserId(userId);

        // (옵션) refresh 토큰을 쿠키로도 내려주고 싶을 경우
        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        boolean secure  = Boolean.TRUE.equals(jwtProps.cookie().secure());
        String sameSite = jwtProps.cookie().sameSite();
        String cookieDomain = cookieDomainFor(req);
        CookieUtil.addCookie(
                res,
                jwtProps.refreshCookieName(),
                t.refreshToken(),
                cookieDomain,
                true,
                secure,
                sameSite,
                maxAge
        );

        return ResponseEntity.ok(new AuthExchangeResponse(
                true, t.accessToken(), t.accessExpEpochSec(), t.refreshExpEpochSec()
        ));
    }

    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null;
    }
}
