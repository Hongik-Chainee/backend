// src/main/java/com/chaineeproject/chainee/service/UserService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("해당 사용자를 찾을 수 없습니다. ID: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new java.util.NoSuchElementException("해당 이메일의 사용자를 찾을 수 없습니다. email: " + email));
    }

    public User findByDid(String did) {
        return userRepository.findByDid(did)
                .orElseThrow(() -> new java.util.NoSuchElementException("해당 DID의 사용자를 찾을 수 없습니다. did: " + did));
    }
}
