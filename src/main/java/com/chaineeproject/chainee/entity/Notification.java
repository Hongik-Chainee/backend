package com.chaineeproject.chainee.entity;

import com.chaineeproject.chainee.entity.enums.NotificationType;
import jakarta.annotation.Resource;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient_created_at", columnList = "recipient_id, created_at DESC"),
        @Index(name = "idx_notification_unread", columnList = "recipient_id, is_read")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림 받는 사람 (공고 작성자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // 알림을 유발한 사용자 (지원자) — 선택
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false)
    private String title;      // 예: "새 지원이 도착했어요"

    @Column(name = "message", nullable = false)
    private String message;    // 예: "홍길동님이 <백엔드 개발자>에 지원했습니다."

    @Column(name = "link_url")
    private String linkUrl;    // 예: "/jobs/101/applications/555"

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

}
