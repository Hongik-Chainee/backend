// src/main/java/com/chaineeproject/chainee/security/SecurityUtils.java
package com.chaineeproject.chainee.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtils {
    private SecurityUtils() {}

    /**
     * Jwt → 사용자 ID
     * - 우선 uid (숫자) 클레임 사용
     * - 없으면 sub를 Long으로 변환
     * - 실패 시 null
     */
    public static Long uidOrNull(Jwt jwt) {
        if (jwt == null) return null;
        try {
            Object u = jwt.getClaim("uid");
            if (u instanceof Number n) return n.longValue();
            String sub = jwt.getSubject();
            if (sub == null || sub.isBlank()) return null;
            return Long.parseLong(sub);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * (하위호환) Authentication → 사용자 ID
     * - principal이 Jwt면 uidOrNull(jwt)
     * - OAuth2User면 attributes의 "id" 시도
     * - UserDetails만 있으면(예: username=email) 필요 시 서비스에서 이메일→ID 매핑
     */
    public static Long extractUserIdOrNull(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object p = auth.getPrincipal();

        // 0) Jwt Principal
        if (p instanceof Jwt jwt) {
            return uidOrNull(jwt);
        }

        // 1) 커스텀 Principal(getId())가 있다면 여기서 처리
        // if (p instanceof com.chaineeproject.chainee.security.UserPrincipal up) return up.getId();

        // 2) OAuth2User: attributes에 id가 있으면 사용
        if (p instanceof OAuth2User ou) {
            Object id = ou.getAttribute("id");
            if (id instanceof Number n) return n.longValue();
            if (id instanceof String s) {
                try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
            }
        }

        // 3) UserDetails(username=email)만 있는 경우 → 필요 시 이메일→ID 매핑(서비스 단)
        if (p instanceof UserDetails) {
            // 예) return userRepository.findIdByEmail(((UserDetails) p).getUsername()).orElse(null);
            return null;
        }

        // 4) details에 Jwt가 들어오는 경우 대비
        Object d = auth.getDetails();
        if (d instanceof Jwt jwt2) return uidOrNull(jwt2);

        return null;
    }
}
