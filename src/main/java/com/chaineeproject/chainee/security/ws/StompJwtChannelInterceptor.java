package com.chaineeproject.chainee.security.ws;

import com.chaineeproject.chainee.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            // 1) Authorization: Bearer xxx or 2) Handshake에서 세팅한 uid 사용
            String auth = acc.getFirstNativeHeader("Authorization");
            if (auth != null && auth.toLowerCase().startsWith("bearer ")) {
                String token = auth.substring(7);
                Jwt jwt = jwtDecoder.decode(token);
                Long uid = SecurityUtils.uidFromJwt(jwt);
                acc.setUser(new WsPrincipal(uid));
            } else {
                Object uidAttr = acc.getSessionAttributes() != null ? acc.getSessionAttributes().get("uid") : null;
                if (uidAttr instanceof Long uid) {
                    acc.setUser(new WsPrincipal(uid));
                } else {
                    throw new AccessDeniedException("STOMP_CONNECT_NO_JWT");
                }
            }
        }

        return message;
    }
}
