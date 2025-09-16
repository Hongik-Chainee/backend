package com.chaineeproject.chainee.security.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class CookieUtil {
    public static Cookie getCookie(HttpServletRequest request, String name) {
        var cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) if (name.equals(c.getName())) return c;
        return null;
    }

    public static void addCookie(HttpServletResponse response, String name, String value,
                                 String domain, boolean httpOnly, boolean secure,
                                 String sameSite, int maxAgeSec) {
        Cookie c = new Cookie(name, value);
        c.setPath("/");
        c.setMaxAge(maxAgeSec);
        if (domain != null && !domain.isBlank()) c.setDomain(domain);
        c.setHttpOnly(httpOnly);
        c.setSecure(secure);
        // SameSite는 표준 API가 없어 헤더 추가
        response.addHeader("Set-Cookie",
                name + "=" + value +
                        "; Path=/" +
                        (domain != null ? "; Domain=" + domain : "") +
                        "; Max-Age=" + maxAgeSec +
                        (secure ? "; Secure" : "") +
                        (httpOnly ? "; HttpOnly" : "") +
                        "; SameSite=" + sameSite);
    }

    public static void deleteCookie(HttpServletResponse response, String name, String domain) {
        addCookie(response, name, "", domain, true, true, "Lax", 0);
    }
}
