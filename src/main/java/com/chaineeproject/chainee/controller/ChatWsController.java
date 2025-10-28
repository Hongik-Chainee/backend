// src/main/java/com/chaineeproject/chainee/controller/ChatWsController.java
package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.entity.Message;
import com.chaineeproject.chainee.security.ws.WsPrincipal;
import com.chaineeproject.chainee.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public record WsSendPayload(Long conversationId, String content, String attachmentUrl) {}

    /**
     * 클라이언트 송신 목적지: /app/chat.send
     * 구독 채널: /topic/conversations/{conversationId}
     */
    @MessageMapping("/chat.send")
    public void send(@Payload WsSendPayload p, Principal principal) {
        Long senderId = extractUid(principal);
        Message saved = chatService.sendMessage(p.conversationId(), senderId, p.content(), p.attachmentUrl());
        messagingTemplate.convertAndSend("/topic/conversations/" + p.conversationId(), saved);
    }

    private Long extractUid(Principal principal) {
        if (principal instanceof WsPrincipal ws) return ws.userId();
        throw new SecurityException("NO_AUTH");
    }
}
