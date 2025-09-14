package com.chaineeproject.chainee.kyc;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SolapiClient {

    @Value("${solapi.api-key}")
    private String apiKey;          // v4 HMAC 전용 apiKey (정확히 16자)
    @Value("${solapi.api-secret}")
    private String apiSecret;       // v4 HMAC 전용 secret
    @Value("${solapi.from-number}")
    private String fromNumber;      // 숫자만(010...)

    private static final String SEND_URL = "https://api.solapi.com/messages/v4/send-many/detail";

    public void sendVerificationCode(String to, String code, String serviceName) {
        // --- 사전 검증 ---
        if (apiKey == null || apiKey.length() != 16)
            throw new IllegalStateException("solapi.api-key must be v4 key (length 16).");
        if (apiSecret == null || apiSecret.isBlank())
            throw new IllegalStateException("solapi.api-secret missing");
        if (fromNumber == null || fromNumber.isBlank())
            throw new IllegalStateException("solapi.from-number missing");
        String toDigits = to.replaceAll("\\D", "");
        String fromDigits = fromNumber.replaceAll("\\D", "");

        // --- HMAC 인증 헤더 생성 ---
        String date = Instant.now().toString();                    // UTC ISO-8601
        String salt = UUID.randomUUID().toString().replace("-", ""); // 12~64자 랜덤
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

        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> res = rt.exchange(
                SEND_URL, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Solapi 전송 실패: " + res.getStatusCode() + " " + res.getBody());
        }
    }

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
}
