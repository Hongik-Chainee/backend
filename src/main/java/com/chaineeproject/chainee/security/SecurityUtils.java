package com.chaineeproject.chainee.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Long extractUserIdOrNull(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object p = auth.getPrincipal();

        // 1) 만약 프로젝트에 커스텀 Principal(예: LoginUser, UserPrincipal 등)에 getId()가 있다면 여기서 처리
        // if (p instanceof com.chaineeproject.chainee.security.UserPrincipal up) return up.getId();

        // 2) OAuth2 로그인 사용 시: id가 attributes에 실려온 경우
        if (p instanceof OAuth2User ou) {
            Object id = ou.getAttribute("id");       // 백엔드에서 심어줬다면
            if (id instanceof Number n) return n.longValue();
            if (id instanceof String s) {
                try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
            }
            // OIDC의 sub(문자열)만 있는 경우 등은 여기서 null 반환
        }

        // 3) UserDetails(username=email)만 있는 경우 → 필요하면 이메일로 DB 조회해서 id 찾기(선택)
        if (p instanceof UserDetails ud) {
            // ex) return userRepository.findIdByEmail(ud.getUsername()).orElse(null);
            return null;
        }

        return null;
    }
}
