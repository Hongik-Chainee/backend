package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.repository.UserRepository;
import com.chaineeproject.chainee.security.GoogleUserDetails;
import com.chaineeproject.chainee.security.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2 user attributes: {}", oAuth2User.getAttributes());

        // 1. 플랫폼 정보 추출 (e.g. "google")
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = new GoogleUserDetails(oAuth2User.getAttributes());

        String email = userInfo.getEmail();
        boolean userExists = userRepository.existsByEmail(email);

        // 2. 회원 존재 여부에 따라 예외 던지기 → 이후 FailureHandler에서 처리
        if (userExists) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("EMAIL_EXISTS"),
                    "가입된 회원입니다. 비밀번호 입력으로 진행하세요.");
        } else {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("EMAIL_AUTH_CODE_SENT"),
                    "가입되지 않은 회원입니다. 회원가입으로 진행하세요.");
        }
    }
}
