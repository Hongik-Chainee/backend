package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import com.chaineeproject.chainee.security.CustomOauth2UserDetails;
import com.chaineeproject.chainee.security.GoogleUserDetails;
import com.chaineeproject.chainee.security.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

//JPA로 사용자 정보 조회/저장
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2 user attributes: {}", oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId(); // 예: "google"
        OAuth2UserInfo userInfo = new GoogleUserDetails(oAuth2User.getAttributes());

        String providerId = userInfo.getProviderId();
        String email = userInfo.getEmail();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            // 신규 사용자 자동 회원가입
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            newUser.setDid("temp-did-" + providerId); // TODO: 이후 DID 인증 후 업데이트
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(newUser);
        });

        return new CustomOauth2UserDetails(user, oAuth2User.getAttributes());
    }
}
