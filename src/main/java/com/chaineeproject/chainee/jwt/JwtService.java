// src/main/java/com/chaineeproject/chainee/auth/JwtService.java
package com.chaineeproject.chainee.jwt;

import com.chaineeproject.chainee.auth.KeyProvider;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final KeyProvider keyProvider;
    private final JwtProperties props;

    public String createAccessToken(Long uid, String email, boolean kyc, boolean did, long ttlSeconds) {
        var now = Instant.now();
        var exp = now.plusSeconds(ttlSeconds);

        var claims = new JWTClaimsSet.Builder()
                .issuer(props.issuer())
                .audience(props.audience())
                .subject(String.valueOf(uid))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("uid", uid)
                .claim("email", email)
                .claim("kyc", kyc)
                .claim("did", did)
                .build();

        var jwk = keyProvider.currentKey();
        var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(jwk.getKeyID()).type(JOSEObjectType.JWT).build();

        try {
            var signed = new SignedJWT(header, claims);
            signed.sign(new RSASSASigner(jwk.toRSAKey()));
            return signed.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("ACCESS_TOKEN_SIGN_FAILED", e);
        }
    }

    public String createRefreshToken(Long uid, long ttlSeconds) {
        var now = Instant.now();
        var exp = now.plusSeconds(ttlSeconds);

        var claims = new JWTClaimsSet.Builder()
                .issuer(props.issuer())
                .audience(props.audience())
                .subject(String.valueOf(uid))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("typ", "refresh")
                .claim("uid", uid)
                .build();

        var jwk = keyProvider.currentKey();
        var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(jwk.getKeyID()).type(JOSEObjectType.JWT).build();

        try {
            var signed = new SignedJWT(header, claims);
            signed.sign(new RSASSASigner(jwk.toRSAKey()));
            return signed.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("REFRESH_TOKEN_SIGN_FAILED", e);
        }
    }
}
