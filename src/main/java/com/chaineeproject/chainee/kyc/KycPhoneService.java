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

    /** ===== 테스트(고정코드) 옵션 ===== */
    @Value("${kyc.test.enabled:false}")     private boolean testEnabled;      // 기본: 비활성
    @Value("${kyc.test.phone:}")            private String testPhone;         // 비우면 모든 번호 적용
    @Value("${kyc.test.code:}")             private String testCode;          // 예: 376406
    @Value("${kyc.test.send-sms:false}")    private boolean testSendSms;      // true면 테스트 번호에도 실제 발송
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

        // dev 프로필 & test.enabled=true & (test.phone 미지정 또는 일치) & test.code 존재 → 고정코드 사용
        boolean matchPhone = (testPhone == null || testPhone.isBlank())
                || normalize(phone).equals(normalize(testPhone));
        boolean useFixed = isDev && testEnabled && matchPhone
                && testCode != null && !testCode.isBlank();

        String code = useFixed ? testCode : random6();

        // 테스트가 아니거나, 테스트라도 실제 발송 요구 시에만 Solapi 호출
        if (!useFixed || testSendSms) {
            solapiClient.sendVerificationCode(phone, code, serviceName);
        }

        // 개발 편의: dev일 때만 코드 로깅
        if (isDev) log.warn("[DEV] KYC code for {} -> {}", phone, code);

        KycSession sess = KycSession.builder()
                .user(user)
                .phone(phone)
                .codeHash(sha256(code))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();
        kycRepo.save(sess);
        return sess.getId();
    }

    @Transactional
    public boolean verifyCode(User user, String requestId, String code) {
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
        user.setKycVerified(true);
        user.setKycPhone(sess.getPhone());
        user.setKycVerifiedAt(LocalDateTime.now());
        userRepo.save(user);
        return true;
    }
}
