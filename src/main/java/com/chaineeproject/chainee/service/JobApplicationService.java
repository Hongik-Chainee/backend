package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.entity.*;
import com.chaineeproject.chainee.entity.enums.NotificationType;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationService {

    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final NotificationRepository notificationRepository; // ✅ 알림 직접 저장

    /**
     * JWT uid = applicantId
     */
    @Transactional
    public void applyToJob(Long postId, Long applicantId, Long resumeId) {
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.APPLICANT_NOT_FOUND));

        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_POST_NOT_FOUND));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESUME_NOT_FOUND));
        if (resume.getOwner() == null || !resume.getOwner().getId().equals(applicantId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        if (jobApplicationRepository.existsByPostIdAndApplicantId(postId, applicantId)) {
            throw new ApplicationException(ErrorCode.DUPLICATE_APPLICATION);
        }

        JobApplication application = new JobApplication();
        application.setPost(post);
        application.setApplicant(applicant);
        application.setResume(resume);
        application.setStatus("pending");
        application.setCreatedAt(LocalDateTime.now());
        jobApplicationRepository.save(application);

        jobPostRepository.syncApplicantCount(postId);

        // ✅ 지원 저장 직후 바로 알림 생성 (이벤트/트랜잭션 훅 의존 X)
        Notification noti = Notification.builder()
                .recipient(post.getAuthor()) // 공고 작성자
                .actor(applicant)            // 지원자
                .notificationType(NotificationType.JOB_APPLICATION_RECEIVED)
                .title("새 지원이 도착했어요")
                .message(String.format("%s님이 \"%s\" 공고에 지원했습니다.",
                        nz(applicant.getName(), "익명"),
                        nz(post.getTitle(), "공고")))
                .linkUrl("/jobs/" + postId + "/applications/" + application.getId())
                .build();
        notificationRepository.save(noti);

        log.debug("Application {} created; Notification {} saved.", application.getId(), noti.getId());
    }

    private static String nz(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }
}
