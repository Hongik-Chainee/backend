package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.NotificationView;
import com.chaineeproject.chainee.dto.PagedNotificationsResponse;
import com.chaineeproject.chainee.entity.Notification;
import com.chaineeproject.chainee.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ===== 조회/읽음 처리 =====
    @Transactional(readOnly = true)
    public PagedNotificationsResponse list(Long currentUserId, int page, int size) {
        Page<Notification> p = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                currentUserId, PageRequest.of(page, size));
        return new PagedNotificationsResponse(
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize(),
                p.getContent().stream().map(this::toView).toList()
        );
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long currentUserId) {
        return notificationRepository.countByRecipientIdAndReadFalse(currentUserId);
    }

    @Transactional
    public void markRead(Long currentUserId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));
        if (!n.getRecipient().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인 알림만 읽음 처리할 수 있습니다.");
        }
        n.setRead(true);
    }

    @Transactional
    public void markAllRead(Long currentUserId) {
        int page = 0, size = 200;
        Page<Notification> p;
        do {
            p = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                    currentUserId, PageRequest.of(page, size));
            p.forEach(n -> n.setRead(true));
            page++;
        } while (!p.isEmpty());
    }

    private NotificationView toView(Notification n) {
        return new NotificationView(
                n.getId(),
                n.getNotificationType().name(),
                n.getTitle(),
                n.getMessage(),
                n.getLinkUrl(),
                n.isRead(),
                n.getCreatedAt(),
                n.getActor() != null ? n.getActor().getId() : null,
                n.getActor() != null ? n.getActor().getName() : null,
                n.getActor() != null ? n.getActor().getProfileImageUrl() : null
        );
    }
}
