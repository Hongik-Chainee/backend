package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.NotificationView;
import com.chaineeproject.chainee.dto.PagedNotificationsResponse;
import com.chaineeproject.chainee.entity.*;
import com.chaineeproject.chainee.entity.enums.NotificationType;
import com.chaineeproject.chainee.event.JobApplicationCreatedEvent;
import com.chaineeproject.chainee.repository.JobApplicationRepository;
import com.chaineeproject.chainee.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JobApplicationRepository jobApplicationRepository;

    // ===== 알림 생성: 지원 발생 시(커밋 이후) =====
    @TransactionalEventListener(fallbackExecution = false) // default BEFORE_COMMIT, 아래서 phase 지정
    public void onJobApplicationCreated_BEFORE_COMMIT(JobApplicationCreatedEvent ignored) {
        // no-op: 문서화 목적. 실제 생성은 AFTER_COMMIT에서.
    }

    @TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void onJobApplicationCreated(JobApplicationCreatedEvent event) {
        jobApplicationRepository.findWithPostAndUsersById(event.applicationId()).ifPresent(app -> {
            User author = app.getPost().getAuthor();
            User applicant = app.getApplicant();

            String title = "새 지원이 도착했어요";
            String message = String.format("%s님이 \"%s\" 공고에 지원했습니다.",
                    nullSafe(applicant.getName(), "익명"), nullSafe(app.getPost().getTitle(), "공고"));

            // 프론트 라우트에 맞게 링크 구성 (예시)
            String link = "/jobs/" + app.getPost().getId() + "/applications/" + app.getId();

            Notification noti = Notification.builder()
                    .recipient(author)
                    .actor(applicant)
                    .type(NotificationType.JOB_APPLICATION_RECEIVED)
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
        // 간단 구현: 페이지로 가져와 일괄 setRead(true)
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
                n.getType().name(),
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
