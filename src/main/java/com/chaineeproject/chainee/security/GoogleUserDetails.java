package com.chaineeproject.chainee.security;

import java.util.Map;

//구글 사용자 정보를 추출하는 클래스
public class GoogleUserDetails implements OAuth2UserInfo{

    private final Map<String, Object> attributes;

    public GoogleUserDetails(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
