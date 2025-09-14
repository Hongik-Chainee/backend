package com.chaineeproject.chainee.security;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2 user attributes: {}", oAuth2User.getAttributes());

        OAuth2UserInfo userInfo = new GoogleUserDetails(oAuth2User.getAttributes());
        String email = userInfo.getEmail();

        Optional<User> userOptional = userRepository.findByEmail(email);
        boolean isNewUser = false;
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = User.builder()
                    .email(userInfo.getEmail())
                    .provider(userInfo.getProvider())
                    .providerId(userInfo.getProviderId())
                    .kycVerified(false)
                    .did(null)
                    .build();
            userRepository.save(user);
            isNewUser = true;
        }

        return new CustomOauth2UserDetails(user, oAuth2User.getAttributes(), isNewUser);
    }
}
