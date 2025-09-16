package com.chaineeproject.chainee.auth;

import com.chaineeproject.chainee.jwt.JwtProperties;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.jwt.JwtService;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshRepo;
    private final UserRepository userRepo;
    private final JwtProperties jwtProps;

    private static String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var b = md.digest(s.getBytes());
            var sb = new StringBuilder();
            for (byte x: b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @Transactional
    public AuthTokens issueFor(User u) {
        long accessTtl  = jwtProps.accessTtl().toSeconds();
        long refreshTtl = jwtProps.refreshTtl().toSeconds();

        String access = jwtService.createAccessToken(
                u.getId(), u.getEmail(),
                u.isKycVerified(),
                u.getDid()!=null && !u.getDid().isBlank(),
                accessTtl);

        String refresh = jwtService.createRefreshToken(u.getId(), refreshTtl);

        var now = Instant.now();
        var rt = RefreshToken.builder()
                .userId(u.getId())
                .tokenHash(sha256(refresh))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshTtl))
                .revoked(false)
                .build();
        refreshRepo.save(rt);

        return new AuthTokens(access, now.plusSeconds(accessTtl).getEpochSecond(),
                refresh, now.plusSeconds(refreshTtl).getEpochSecond());
    }

    @Transactional
    public AuthTokens rotate(String refreshToken) {
        var rt = refreshRepo.findByTokenHashAndRevokedFalse(sha256(refreshToken))
                .orElseThrow(() -> new IllegalStateException("INVALID_REFRESH"));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("REFRESH_EXPIRED");
        }
        var u = userRepo.findById(rt.getUserId()).orElseThrow();
        rt.setRevoked(true);
        return issueFor(u);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshRepo.findByTokenHashAndRevokedFalse(sha256(refreshToken))
                .ifPresent(rt -> rt.setRevoked(true));
    }
}
