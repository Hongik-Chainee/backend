package com.chaineeproject.chainee.kyc;

import com.chaineeproject.chainee.auth.AuthService;
import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import com.chaineeproject.chainee.security.oauth.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycPhoneService kycPhoneService;
    private final UserRepository userRepo;

    // ⬇️ 추가
    private final AuthService authService;
    private final JwtProperties jwtProps;

    private User me(Jwt jwt) {
        Long id = Long.valueOf(jwt.getSubject()); // sub=uid (문자열)
        return userRepo.findById(id).orElseThrow();
    }

    @PostMapping("/phone/request")
    public Map<String,Object> request(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String,String> body){
        String phone = body.get("phone");
        String requestId = kycPhoneService.requestCode(me(jwt), phone, "Chainee");
        return Map.of("requestId", requestId, "sent", true);
    }

    // ✅ 옵션 B: 성공 시 즉시 access+refresh 재발급
    @PostMapping("/phone/verify")
    public ResponseEntity<?> verify(@AuthenticationPrincipal Jwt jwt,
                                    @RequestBody Map<String,String> body,
                                    HttpServletResponse res) {
        User user = me(jwt);
        boolean ok = kycPhoneService.verifyCode(user, body.get("requestId"), body.get("code"));
        if (!ok) {
            return ResponseEntity.ok(Map.of(
                    "kycVerified", false,
                    "nextStep", "RETRY"
            ));
        }

        // (서비스 내부에서 user.kycVerified=true 저장을 했다고 가정.
        //  만약 안 했다면 여기서 user.setKycVerified(true); userRepo.save(user);)

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

        return ResponseEntity.ok(Map.of(
                "kycVerified", true,
                "nextStep", "DID",
                "accessToken", tokens.accessToken(),
                "accessExp", tokens.accessExpEpochSec(),
                "refreshExp", tokens.refreshExpEpochSec()
        ));
    }
}
