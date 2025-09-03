package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "알림 항목")
public record NotificationView(
        Long id,
        String type,
        String title,
        String message,
        String linkUrl,
        boolean read,
        LocalDateTime createdAt,
        Long actorId,
        String actorName,
        String actorProfileImageUrl
) {}
