// src/main/java/com/chaineeproject/chainee/controller/DidController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/did")
@RequiredArgsConstructor
public class DidController {

    private final UserRepository users;
    private final AuthService authService;
    private final JwtProperties jwtProps;

    // 재사용 가능한 보안 난수
    private static final SecureRandom RAND = new SecureRandom();

    @PostMapping("/nonce")
    public Map<String, Object> nonce(@AuthenticationPrincipal Jwt jwt) {
        Long uid = Long.valueOf(jwt.getSubject()); // sub = uid
        User me = users.findById(uid).orElseThrow();

        String nonce = genNonce();
        me.setDidNonce(nonce);
        me.setDidNonceExpiresAt(LocalDateTime.now().plusMinutes(10));
        users.save(me);

        return Map.of("nonce", nonce, "expiresInSec", 600);
    }

    public record DidVerifyReq(String did, String address, String signatureBase58) {}

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@AuthenticationPrincipal Jwt jwt,
                                    @RequestBody DidVerifyReq req,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        Long uid = Long.valueOf(jwt.getSubject());
        User me = users.findById(uid).orElseThrow();

        // 1) Nonce 유효성 확인
        if (me.getDidNonce() == null || me.getDidNonceExpiresAt() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "messageCode", "NONCE_MISSING"));
        }
        if (LocalDateTime.now().isAfter(me.getDidNonceExpiresAt())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "messageCode", "NONCE_EXPIRED"));
        }

        // 2) 솔라나 서명 검증 (address=Base58 public key, signature=Base58)
        boolean ok = verifySolanaSignature(req.address(), req.signatureBase58(), me.getDidNonce());
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "messageCode", "SIGNATURE_INVALID"));
        }

        // 3) DID 저장 + Nonce 정리
        me.setDid(req.did());
        me.setDidNonce(null);
        me.setDidNonceExpiresAt(null);
        users.save(me);

        // 4) 새 토큰 세트 재발급 (did=true 반영)
        var tokens = authService.issueFor(me);

        // 5) Refresh 쿠키 교체 (운영/로컬 도메인 분기)
        int maxAge = (int) jwtProps.refreshTtl().toSeconds();
        String cookieDomain = cookieDomainFor(request);
        CookieUtil.addCookie(
                response,
                jwtProps.refreshCookieName(),
                tokens.refreshToken(),
                cookieDomain,
                true,                                               // HttpOnly
                Boolean.TRUE.equals(jwtProps.cookie().secure()),    // Secure
                jwtProps.cookie().sameSite(),                       // SameSite
                maxAge
        );

        // 6) Access 바디 반환
        return ResponseEntity.ok(Map.of(
                "success", true,
                "accessToken", tokens.accessToken(),
                "accessExp", tokens.accessExpEpochSec(),
                "refreshExp", tokens.refreshExpEpochSec()
        ));
    }

    /* ===================== 유틸 ===================== */

    // URL-safe Base64 nonce (24바이트 = 192비트)
    private String genNonce() {
        byte[] buf = new byte[24];
        RAND.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    // 운영 도메인에만 Domain=chainee.store 지정, 로컬/개발은 null(브라우저 저장 안 막히게)
    private String cookieDomainFor(HttpServletRequest req) {
        String host = req.getServerName();
        if (host != null && (host.equals("chainee.store") || host.endsWith(".chainee.store"))) {
            return "chainee.store";
        }
        return null;
    }

    /**
     * Solana(Ed25519) 서명 검증.
     * - addressBase58: Base58 인코딩된 32바이트 공개키(계정 주소)
     * - sigBase58    : Base58 인코딩된 64바이트 서명
     * - message      : 서버가 발급한 nonce (원문 그대로 바이트 변환하여 검증)
     */
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

    /**
     * 간단한 Base58 디코더 (Bitcoin/Solana 알파벳).
     * 외부 라이브러리 없이 사용 가능하도록 포함.
     */
    private static final class Base58 {
        // Alphabet: '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz'
        private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        private static final int[] INDEXES = new int[128];
        static {
            for (int i = 0; i < INDEXES.length; i++) INDEXES[i] = -1;
            for (int i = 0; i < ALPHABET.length(); i++) INDEXES[ALPHABET.charAt(i)] = i;
        }

        static byte[] decode(String input) {
            if (input == null || input.isEmpty()) return new byte[0];

            // Count leading zeros.
            int zeros = 0;
            int length = 0;
            for (int i = 0; i < input.length() && input.charAt(i) == '1'; i++) zeros++;

            // Base58 decoding
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

            // Skip leading zeros in temp.
            int start = input.length() - length;
            while (start < input.length() && temp[start] == 0) start++;

            // Result.
            int resultLen = zeros + (input.length() - start);
            byte[] decoded = new byte[resultLen];
            for (int i = 0; i < zeros; i++) decoded[i] = 0;
            int idx = zeros;
            for (int i = start; i < input.length(); i++) decoded[idx++] = temp[i];
            return decoded;
        }
    }
}
