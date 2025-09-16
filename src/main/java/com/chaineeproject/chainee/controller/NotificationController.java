package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.PagedNotificationsResponse;
import com.chaineeproject.chainee.dto.NotificationView;
import com.chaineeproject.chainee.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.chaineeproject.chainee.security.SecurityUtils.extractUserIdOrNull;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
            summary = "내 알림 목록",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedNotificationsResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalElements": 1,
                              "totalPages": 1,
                              "page": 0,
                              "size": 10,
                              "notifications": [
                                {
                                  "id": 9001,
                                  "type": "JOB_APPLICATION_RECEIVED",
                                  "title": "새 지원이 도착했어요",
                                  "message": "홍길동님이 \\"백엔드 개발자\\" 공고에 지원했습니다.",
                                  "linkUrl": "/jobs/101/applications/555",
                                  "read": false,
                                  "createdAt": "2025-09-02T10:00:00",
                                  "actorId": 3,
                                  "actorName": "홍길동",
                                  "actorProfileImageUrl": "https://cdn.example.com/u/3.png"
                                }
                              ]
                            }
                            """)
                    )
            )
    )
    public ResponseEntity<PagedNotificationsResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long currentUserId = extractUserIdOrNull(authentication);
        if (currentUserId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(notificationService.list(currentUserId, page, size));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "미읽음 개수")
    public ResponseEntity<Long> unreadCount(Authentication authentication) {
        Long currentUserId = extractUserIdOrNull(authentication);
        if (currentUserId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(notificationService.unreadCount(currentUserId));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "단건 읽음 처리")
    public ResponseEntity<Void> markRead(Authentication authentication, @PathVariable Long notificationId) {
        Long currentUserId = extractUserIdOrNull(authentication);
        if (currentUserId == null) return ResponseEntity.status(401).build();
        notificationService.markRead(currentUserId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "모두 읽음 처리")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        Long currentUserId = extractUserIdOrNull(authentication);
        if (currentUserId == null) return ResponseEntity.status(401).build();
        notificationService.markAllRead(currentUserId);
        return ResponseEntity.noContent().build();
    }
}
