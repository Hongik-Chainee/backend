package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.did.dto.DidNonceResponse;
import com.chaineeproject.chainee.did.dto.DidVerifyRequest;
import com.chaineeproject.chainee.did.dto.DidVerifyResponse;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.repository.UserRepository;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/did")
@RequiredArgsConstructor
@Tag(name = "DID", description = "DID(지갑) 연동/검증 API")
@SecurityRequirement(name = "bearerAuth")
public class DidController {

    private final UserRepository users;
    private final AuthService authService;
    private final JwtProperties jwtProps;

    private static final SecureRandom RAND = new SecureRandom();

    @Operation(
            summary = "DID용 서명 nonce 발급",
            description = "지갑에서 서명할 랜덤 nonce를 발급합니다. 이 nonce를 그대로 메시지로 서명한 뒤 /api/did/verify에 제출하세요.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "발급 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DidNonceResponse.class),
                            examples = @ExampleObject(
                                    name = "예시",
                                    value = """
                                            { "nonce": "6y7B8yB0W2bN8asbO5zQwH1P7C0s3-cUZG2p1KcB9ko", "expiresInSec": 600 }
                                            """
                            )
                    )
            )
    )
    @PostMapping(value = "/nonce", produces = MediaType.APPLICATION_JSON_VALUE)
    public DidNonceResponse nonce(@AuthenticationPrincipal Jwt jwt) {
        Long uid = Long.valueOf(jwt.getSubject());
        User me = users.findById(uid).orElseThrow();

        String nonce = genNonce();
        me.setDidNonce(nonce);
        me.setDidNonceExpiresAt(LocalDateTime.now().plusMinutes(10));
        users.save(me);

        return new DidNonceResponse(nonce, 600);
    }

    @Operation(
            summary = "DID 검증",
            description = """
                    1) /api/did/nonce로 받은 nonce를 그대로 메시지로 사용해, Solana(Ed25519) 개인키로 서명하세요.
                    2) 지갑 주소(Base58 PublicKey), DID, 서명(Base58)을 본 API에 제출하면 서명을 검증하고 DID를 계정에 연결합니다.
                    3) 성공 시 did=true가 반영된 새로운 access/refresh 토큰을 발급합니다.
                    """,
            // ★ 여기! alias 없이 완전수식명 사용
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DidVerifyRequest.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "did": "did:sol:6h5Z5o3P8TQfYd1wL2S3R8mV9PqKj8w2uB3F",
                                              "address": "3ypq3qGxhw6sSpH3zv3C8q3vG6iZ3vR3FJ7mJYwRkQ6o",
                                              "signatureBase58": "5dVjE3gD4mUXo8wPswj7H7w8f8n3zq3b2b...WmLcp"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검증 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DidVerifyResponse.class),
                                    examples = @ExampleObject(
                                            name = "성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "accessExp": 1760091753,
                                                      "refreshExp": 1761300453
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "검증 실패(Nonce 누락/만료/서명 불일치 등)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(example = """
                                            { "success": false, "messageCode": "SIGNATURE_INVALID" }
                                            """)
                            )
                    )
            }
    )
    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verify(@AuthenticationPrincipal Jwt jwt,
                                    @RequestBody DidVerifyRequest req,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        Long uid = Long.valueOf(jwt.getSubject());
        User me = users.findById(uid).orElseThrow();

        // 1) Nonce 유효성
        if (me.getDidNonce() == null || me.getDidNonceExpiresAt() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "messageCode", "NONCE_MISSING"));
        }
        if (LocalDateTime.now().isAfter(me.getDidNonceExpiresAt())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "messageCode", "NONCE_EXPIRED"));
        }

        // 2) 서명 검증
        boolean ok = verifySolanaSignature(req.address(), req.signatureBase58(), me.getDidNonce());
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "messageCode", "SIGNATURE_INVALID"));
        }

        // 3) DID 저장 + Nonce 정리
        me.setDid(req.did());
        me.setDidNonce(null);
        me.setDidNonceExpiresAt(null);
        users.save(me);

        // 4) 새 토큰 세트 재발급
        var tokens = authService.issueFor(me);

        // 5) Refresh 쿠키 교체
        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        String cookieDomain = cookieDomainFor(request);
        CookieUtil.addCookie(
                response,
                jwtProps.refreshCookieName(),
                tokens.refreshToken(),
                cookieDomain,
                true,
                Boolean.TRUE.equals(jwtProps.cookie().secure()),
                jwtProps.cookie().sameSite(),
                maxAge
        );

        // 6) Access 바디 반환
        return ResponseEntity.ok(new DidVerifyResponse(
                true,
                tokens.accessToken(),
                tokens.accessExpEpochSec(),
                tokens.refreshExpEpochSec()
        ));
    }

    /* ===================== 유틸 ===================== */

    private String genNonce() {
        byte[] buf = new byte[24];
        RAND.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null;
    }

    private boolean verifySolanaSignature(String addressBase58, String sigBase58, String message) {
        try {
            byte[] pub = Base58.decode(addressBase58); // 32 bytes
            byte[] sig = Base58.decode(sigBase58);     // 64 bytes
            byte[] msg = message.getBytes(StandardCharsets.UTF_8);

            if (pub == null || pub.length != 32) return false;
            if (sig == null || sig.length != 64) return false;

            Ed25519PublicKeyParameters pk = new Ed25519PublicKeyParameters(pub, 0);
            Ed25519Signer verifier = new Ed25519Signer();
            verifier.init(false, pk);
            verifier.update(msg, 0, msg.length);
            return verifier.verifySignature(sig);
        } catch (Exception e) {
            return false;
        }
    }

    private static final class Base58 {
        private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        private static final int[] INDEXES = new int[128];
        static {
            for (int i = 0; i < INDEXES.length; i++) INDEXES[i] = -1;
            for (int i = 0; i < ALPHABET.length(); i++) INDEXES[ALPHABET.charAt(i)] = i;
        }

        static byte[] decode(String input) {
            if (input == null || input.isEmpty()) return new byte[0];

            int zeros = 0;
            int length = 0;
            for (int i = 0; i < input.length() && input.charAt(i) == '1'; i++) zeros++;

            byte[] b58 = new byte[input.length()];
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c >= 128 || INDEXES[c] == -1) throw new IllegalArgumentException("Invalid Base58 character: " + c);
                b58[i] = (byte) INDEXES[c];
            }

            byte[] temp = new byte[input.length()];
            for (int i = 0; i < input.length(); i++) {
                int carry = b58[i] & 0xFF;
                int j = input.length() - 1;
                for (; (carry != 0 || j >= length) && j >= 0; j--) {
                    carry += 58 * (temp[j] & 0xFF);
                    temp[j] = (byte) (carry % 256);
                    carry /= 256;
                }
                length = input.length() - 1 - j;
            }

            int start = input.length() - length;
            while (start < input.length() && temp[start] == 0) start++;

            int resultLen = zeros + (input.length() - start);
            byte[] decoded = new byte[resultLen];
            for (int i = 0; i < zeros; i++) decoded[i] = 0;
            int idx = zeros;
            for (int i = start; i < input.length(); i++) decoded[idx++] = temp[i];
            return decoded;
        }
    }
}
