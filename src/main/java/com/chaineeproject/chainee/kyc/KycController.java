package com.chaineeproject.chainee.kyc;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.security.CustomOauth2UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycPhoneService kycPhoneService;

    @PostMapping("/phone/request")
    public Map<String, Object> request(@AuthenticationPrincipal CustomOauth2UserDetails me,
                                       @RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String requestId = kycPhoneService.requestCode(me.getUser(), phone, "Chainee");
        return Map.of("requestId", requestId, "sent", true);
    }

    @PostMapping("/phone/verify")
    public Map<String, Object> verify(@AuthenticationPrincipal CustomOauth2UserDetails me,
                                      @RequestBody Map<String, String> body) {
        String requestId = body.get("requestId");
        String code = body.get("code");
        boolean ok = kycPhoneService.verifyCode(me.getUser(), requestId, code);
        String nextStep = (ok && (me.getUser().getDid() == null || me.getUser().getDid().isBlank())) ? "DID" : "HOME";
        return Map.of("kycVerified", ok,
                "status", Map.of("kyc", true, "did", me.getUser().getDid()!=null),
                "nextStep", nextStep);
    }
}
