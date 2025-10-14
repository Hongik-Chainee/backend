package com.chaineeproject.chainee.security;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * OAuth2 로그인 시 사용자 정보를 적재/동기화하는 서비스.
 * - User 엔티티는 did 문자열을 보관하지 않음. (didVerified 플래그만 관리)
 * - 신규 회원은 email/provider/providerId만 저장하고, 나머지는 기본값 사용.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2 user attributes: {}", oAuth2User.getAttributes());

        // Google 기반 예시 (필요 시 provider별 분기)
        OAuth2UserInfo userInfo = new GoogleUserDetails(oAuth2User.getAttributes());
        String email = userInfo.getEmail();

        Optional<User> userOptional = userRepository.findByEmail(email);
        boolean isNewUser = false;
        User user;

        if (userOptional.isPresent()) {
            // 기존 사용자: provider 정보 최신화(필요 시)
            user = userOptional.get();
            if (user.getProvider() == null) {
                user.setProvider(userInfo.getProvider());
            }
            if (user.getProviderId() == null) {
                user.setProviderId(userInfo.getProviderId());
            }
            // (옵션) 이름/프로필 이미지 등 동기화가 필요하면 여기서 업데이트
            // user.setName(userInfo.getName());
            // user.setProfileImageUrl(userInfo.getPicture());
        } else {
            // 신규 사용자: 기본값과 함께 저장
            user = User.builder()
                    .email(userInfo.getEmail())
                    .provider(userInfo.getProvider())
                    .providerId(userInfo.getProviderId())
                    .kycVerified(false)     // @PrePersist로도 기본 false지만 명시
                    .didVerified(false)     // @PrePersist로도 기본 false지만 명시
                    .build();
            userRepository.save(user);
            isNewUser = true;
        }

        // CustomOauth2UserDetails(도메인 User, OAuth attributes, isNewUser) 시그니처 가정
        return new CustomOauth2UserDetails(user, oAuth2User.getAttributes(), isNewUser);
    }
}
