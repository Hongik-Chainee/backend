package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.entity.User;

public interface UserService {
    User findById(Long id);
    User findByEmail(String email);
}
