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

    @MessageMapping("/chat.send")
    public void send(@Payload WsSendPayload p, Principal principal) {
        Long senderId = extractUid(principal);
        Message saved = chatService.sendMessage(p.conversationId(), senderId, p.content(), p.attachmentUrl());
        messagingTemplate.convertAndSend("/topic/conversations/" + p.conversationId(), saved);
    }

    private Long extractUid(Principal principal) {
        if (principal instanceof WsPrincipal ws) return ws.userId();
        // 안전망: 그래도 없으면 거부
        throw new SecurityException("NO_AUTH");
    }
}
