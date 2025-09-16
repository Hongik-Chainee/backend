package com.chaineeproject.chainee.auth;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.stereotype.Component;

@Component
public class KeyProvider {
    // 데모용: 메모리 키. 실제 운영은 KMS/Keystore/JKS + 롤링 권장
    private final RSAKey rsaJwk;

    public KeyProvider() {
        try {
            this.rsaJwk = new RSAKeyGenerator(2048)
                    .keyID("chainee-kid-1")
                    .generate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public RSAKey currentKey(){ return rsaJwk; }
    public JWKSet jwkSet(){ return new JWKSet(rsaJwk.toPublicJWK()); }
}
