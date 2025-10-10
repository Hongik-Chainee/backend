package com.chaineeproject.chainee.kyc;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * Solapi v4 SMS Client
 * - HMAC-SHA256 인증
 * - 숫자만 전송
 * - 개발용 mock 모드 지원
 * - 예외를 4xx/5xx로 매핑하여 500로 뭉개지지 않도록 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SolapiClient {

    @Value("${solapi.api-key}")
    private String apiKey;          // v4 HMAC 전용 apiKey (정확히 16자)

    @Value("${solapi.api-secret}")
    private String apiSecret;       // v4 HMAC 전용 secret

    @Value("${solapi.from-number}")
    private String fromNumber;      // 숫자만(010...)

    @Value("${app.sms.mock:false}")
    private boolean mock;           // 개발용: true면 실제 전송 대신 로그만

    private static final String SEND_URL = "https://api.solapi.com/messages/v4/send-many/detail";

    /**
     * 인증번호 전송
     */
    public void sendVerificationCode(String to, String code, String serviceName) {
        // --- 모의 발송 모드 ---
        if (mock) {
            log.info("[SMS-MOCK] to={}, from={}, text=[{}] (실제 전송 안 함)", sanitizePhone(to), sanitizePhone(fromNumber),
                    String.format("[%s] 인증번호 %s", serviceName, code));
            return;
        }

        // --- 사전 검증 ---
        if (apiKey == null || apiKey.length() != 16) {
            throw new IllegalArgumentException("solapi.api-key(v4, 16자) 미설정 또는 길이 불일치");
        }
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalArgumentException("solapi.api-secret 미설정");
        }
        if (fromNumber == null || !fromNumber.matches("\\d{8,}")) {
            throw new IllegalArgumentException("solapi.from-number 미설정(숫자만)");
        }

        String toDigits = sanitizePhone(to);
        String fromDigits = sanitizePhone(fromNumber);

        // --- HMAC 인증 헤더 생성 (v4 규격) ---
        String date = Instant.now().toString();                       // UTC ISO-8601, 서버 시계 오차가 크면 인증 실패
        String salt = UUID.randomUUID().toString().replace("-", "");  // 랜덤 문자열
        String signature = hmacSha256Hex(apiSecret, date + salt);
        String authHeader = String.format(
                "HMAC-SHA256 apiKey=%s, date=%s, salt=%s, signature=%s",
                apiKey, date, salt, signature
        );

        // --- 요청 메시지 ---
        String text = String.format("[%s] 인증번호 %s", serviceName, code);
        Map<String, Object> msg = new HashMap<>();
        msg.put("to", toDigits);
        msg.put("from", fromDigits);
        msg.put("text", text);
        Map<String, Object> body = Map.of("messages", List.of(msg));

        // --- 전송 ---
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> res = rt.exchange(
                    SEND_URL, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            if (!res.getStatusCode().is2xxSuccessful()) {
                // 비정상 상태는 그대로 예외
                throw new IllegalStateException("Solapi 비정상 상태: " + res.getStatusCode() + " " + res.getBody());
            }

            log.info("[SOLAPI] sent to={}, status={}, body={}", toDigits, res.getStatusCode(), res.getBody());

        } catch (RestClientResponseException ex) {
            // Solapi가 보낸 본문/상태를 그대로 확인 가능
            log.warn("[SOLAPI {}] body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());

            int sc = ex.getRawStatusCode();
            // 입력 오류/인증 실패/쿨다운 등은 4xx로 변환
            if (sc == 400 || sc == 401 || sc == 403 || sc == 404 || sc == 409 || sc == 422 || sc == 429) {
                throw new IllegalArgumentException("Solapi 오류(" + sc + "): " + ex.getResponseBodyAsString());
            }
            // 나머지는 서버 오류 취급
            throw new IllegalStateException("Solapi 서버 오류(" + sc + "): " + ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[SOLAPI] unexpected error", e);
            throw e;
        }
    }

    // 숫자만 남김
    private static String sanitizePhone(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }

    // HMAC-SHA256 hex
    private static String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 생성 실패", e);
        }
    }

    @PostConstruct
    void initLog() {
        log.info("[SOLAPI] keyLen={}, hasSecret={}, from={}, mock={}",
                apiKey == null ? null : apiKey.length(),
                apiSecret != null,
                fromNumber,
                mock);
    }
}
