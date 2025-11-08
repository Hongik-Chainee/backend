// src/main/java/com/chaineeproject/chainee/did/DidService.java
package com.chaineeproject.chainee.did;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DidService {

    private final UserRepository userRepository;

    @Transactional
    public User updateDidVerified(User me, boolean verified, String didAddress) {
        if (verified) {
            String normalized = normalizeDid(didAddress);
            validateDid(normalized);
            me.markDidVerified(normalized);
        } else {
            me.unmarkDidVerified();
        }
        return userRepository.save(me);
    }

    /** 필요 시 향후 스킴별 정규화 확장 */
    private String normalizeDid(String did) {
        if (!StringUtils.hasText(did)) return null;
        return did.trim();
    }

    /** 최소 검증: "did:" 프리픽스 확인 + 길이 체크 */
    private void validateDid(String did) {
        if (!StringUtils.hasText(did)) {
            throw new IllegalArgumentException("verified=true 일 때 DID 주소가 필요합니다.");
        }
        if (!did.startsWith("did:")) {
            throw new IllegalArgumentException("유효하지 않은 DID 형식입니다. 'did:'로 시작해야 합니다.");
        }
        if (did.length() > 255) {
            throw new IllegalArgumentException("DID 길이가 너무 깁니다.(최대 255자)");
        }
    }
}
