// src/main/java/com/chaineeproject/chainee/did/DidService.java
package com.chaineeproject.chainee.did;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DidService {

    private final UserRepository userRepository;

    @Transactional
    public User updateDidVerified(User me, boolean verified) {
        if (verified) me.markDidVerified();
        else me.unmarkDidVerified();
        return userRepository.save(me);
    }
}
