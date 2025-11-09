// src/main/java/com/chaineeproject/chainee/service/NotificationService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.NotificationView;
import com.chaineeproject.chainee.dto.PagedNotificationsResponse;
import com.chaineeproject.chainee.entity.Notification;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.entity.enums.NotificationType;
import com.chaineeproject.chainee.event.JobApplicationCreatedEvent;
import com.chaineeproject.chainee.repository.JobApplicationRepository;
import com.chaineeproject.chainee.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JobApplicationRepository jobApplicationRepository;

    // 지원 발생 후 커밋이 완료된 다음 알림 생성
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobApplicationCreated(JobApplicationCreatedEvent event) {
        jobApplicationRepository.findByIdWithPostAndUsers(event.applicationId()) // ✅ 메서드명 변경
                .ifPresent(app -> {
                    User author = app.getPost().getAuthor();
                    User applicant = app.getApplicant();

                    String title = "새 지원이 도착했어요";
                    String message = String.format("%s님이 \"%s\" 공고에 지원했습니다.",
                            nullSafe(applicant.getName(), "익명"),
                            nullSafe(app.getPost().getTitle(), "공고"));

                    String link = "/jobs/" + app.getPost().getId()
                            + "/applications/" + app.getId();

                    Notification noti = Notification.builder()
                            .recipient(author)
                            .actor(applicant)
                            .notificationType(NotificationType.JOB_APPLICATION_RECEIVED)
                            .title(title)
                            .message(message)
                            .linkUrl(link)
                            .build();
                    notificationRepository.save(noti);
                });
    }

    private String nullSafe(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

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
