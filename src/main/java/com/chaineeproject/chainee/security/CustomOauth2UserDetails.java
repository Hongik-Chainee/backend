package com.chaineeproject.chainee.security;

import com.chaineeproject.chainee.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

//로그인 시 사용자 DB 조회 & 없으면 회원가입(로그인 후 인증정보 역할 UserDetails+OAuth2User)
@RequiredArgsConstructor
public class CustomOauth2UserDetails implements OAuth2User, UserDetails {

    private final User user;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getEmail(); // 또는 user.getId().toString()
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 권한이 없으면 비워두기
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash(); // OAuth 사용자는 보통 null
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}