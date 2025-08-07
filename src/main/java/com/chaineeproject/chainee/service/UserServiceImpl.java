package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자를 찾을 수 없습니다. ID: " + id));
    }

    @Override
    public User findByEmail(String email) {
        return null;
    }
}
