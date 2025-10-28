// src/main/java/com/chaineeproject/chainee/dto/chat/ChatConversationSummaryDto.java
package com.chaineeproject.chainee.dto.chat;

import java.time.LocalDateTime;

public record ChatConversationSummaryDto(
        Long id,
        String partnerName,
        String partnerProfileImageUrl,
        String lastMessage,
        LocalDateTime lastMessageAt,
        Integer unreadCount
) {}
