package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.entity.Conversation;
import com.chaineeproject.chainee.entity.Message;
import com.chaineeproject.chainee.service.ChatService;
import com.chaineeproject.chainee.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public record StartReq(Long postId, Long applicantId) {}
    public record SendReq(String content, String attachmentUrl) {}

    @PostMapping("/conversations")
    @Operation(summary = "대화 시작(구인자 전용)")
    public ResponseEntity<Conversation> start(@AuthenticationPrincipal Jwt jwt,
                                              @RequestBody StartReq req) {
        Long uid = SecurityUtils.uidOrNull(jwt);
        if (uid == null) return ResponseEntity.status(401).build();
        var conv = chatService.startConversation(req.postId(), req.applicantId(), uid);
        return ResponseEntity.ok(conv);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "메시지 조회")
    public ResponseEntity<Page<Message>> messages(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable Long conversationId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "50") int size) {
        Long uid = SecurityUtils.uidOrNull(jwt);
        if (uid == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(chatService.getMessages(conversationId, uid, page, size));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "메시지 전송")
    public ResponseEntity<Message> send(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable Long conversationId,
                                        @RequestBody SendReq req) {
        Long uid = SecurityUtils.uidOrNull(jwt);
        if (uid == null) return ResponseEntity.status(401).build();
        var saved = chatService.sendMessage(conversationId, uid, req.content(), req.attachmentUrl());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/conversations/{conversationId}/read")
    @Operation(summary = "읽음 처리")
    public ResponseEntity<Void> read(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable Long conversationId) {
        Long uid = SecurityUtils.uidOrNull(jwt);
        if (uid == null) return ResponseEntity.status(401).build();
        chatService.markRead(conversationId, uid);
        return ResponseEntity.ok().build();
    }
}
