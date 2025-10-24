package com.chaineeproject.chainee.security.ws;

import com.chaineeproject.chainee.security.SecurityUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;

/**
 * ws://.../ws?token=eyJ... 형태로 접속 시, 세션 attribute에 uid 저장
 * (CONNECT 프레임 Authorization 헤더 사용이 원칙이고, 이건 보조 용도)
 */
public class HttpHandshakeJwtInterceptor implements HandshakeInterceptor {

    // 선택적으로 쿼리파라미터 token 처리 (JwtDecoder는 STOMP 인터셉터에서 사용)
    private static JwtDecoder staticDecoder;

    public static void setDecoder(JwtDecoder decoder) {
        staticDecoder = decoder;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=") && staticDecoder != null) {
            String token = Arrays.stream(query.split("&"))
                    .filter(p -> p.startsWith("token="))
                    .map(p -> p.substring("token=".length()))
                    .findFirst().orElse(null);

            if (token != null && !token.isBlank()) {
                Jwt jwt = staticDecoder.decode(token);
                Long uid = SecurityUtils.uidFromJwt(jwt);
                attributes.put("uid", uid);
            }
        }
        return true;
    }

    @Override public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                         WebSocketHandler wsHandler, Exception ex) {}
}
