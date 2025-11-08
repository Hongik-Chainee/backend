package com.chaineeproject.chainee.kyc;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycPhoneService {

    private final SolapiClient solapiClient;
    private final KycSessionRepository kycRepo;
    private final UserRepository userRepo;
    private final Environment env;

    @Value("${kyc.test.enabled:false}")     private boolean testEnabled;
    @Value("${kyc.test.phone:}")            private String testPhone;
    @Value("${kyc.test.code:}")             private String testCode;
    @Value("${kyc.test.send-sms:false}")    private boolean testSendSms;
    @Value("${kyc.code.ttl-seconds:300}")   private long ttlSeconds;
    @Value("${kyc.code.grace-seconds:0}")   private long graceSeconds;

    private String random6() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    private String normalize(String p) { return p == null ? "" : p.replaceAll("\\D", ""); }

    @Transactional
    public String requestCode(User user, String phone, String serviceName) {
        boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");

        boolean matchPhone = (testPhone == null || testPhone.isBlank())
                || normalize(phone).equals(normalize(testPhone));
        boolean useFixed = isDev && testEnabled && matchPhone
                && testCode != null && !testCode.isBlank();

        String code = useFixed ? testCode : random6();

        if (!useFixed || testSendSms) {
            solapiClient.sendVerificationCode(phone, code, serviceName);
        }
        if (isDev) {
            // 번호 전체 로그 금지: 일부만 마스킹
            String masked = phone == null ? null
                    : phone.replaceAll("\\d(?=\\d{2})", "*");
            log.warn("[DEV] KYC code for {} -> {}", masked, code);
        }

        KycSession sess = KycSession.builder()
                .user(user)
                // .phone(phone)  // ❌ 저장하지 않음
                .codeHash(sha256(code))
                .expiresAt(LocalDateTime.now().plusSeconds(ttlSeconds))
                .verified(false)
                .build();
        kycRepo.save(sess);
        return sess.getId();
    }

    @Transactional
    public boolean verifyCode(User user, String requestId, String code, String name) {
        KycSession sess = kycRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요청 ID"));
        if (!sess.getUser().getId().equals(user.getId()))
            throw new IllegalStateException("다른 사용자 요청입니다.");
        if (sess.isVerified()) return true;
        if (LocalDateTime.now().isAfter(sess.getExpiresAt()))
            throw new IllegalStateException("인증번호가 만료되었습니다.");
        if (!sess.getCodeHash().equals(sha256(code)))
            throw new IllegalStateException("인증번호가 일치하지 않습니다.");

        // 통과 처리
        sess.setVerified(true);

        // 이름 저장 정책(변경 없음)
        String trimmed = name == null ? null : name.trim();
        if (trimmed != null && !trimmed.isEmpty()) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(trimmed);
            }
            // 항상 덮어쓰려면 위 조건문을 제거하고 setName만 호출
        }

        user.setKycVerified(true);
        user.setKycVerifiedAt(LocalDateTime.now());
        // user.setKycPhone(sess.getPhone()); // ❌ 제거
        userRepo.save(user);
        return true;
    }
}
