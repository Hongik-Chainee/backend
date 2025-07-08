package com.chaineeproject.chainee.security;

//구글 사용자 정보를 통일된 구조로 추상화
public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();
}
