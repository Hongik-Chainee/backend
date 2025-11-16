// src/main/java/com/chaineeproject/chainee/service/EmployerApplicationService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.*;
import com.chaineeproject.chainee.entity.JobApplication;
import com.chaineeproject.chainee.entity.JobPost;
import com.chaineeproject.chainee.entity.Notification;
import com.chaineeproject.chainee.entity.Resume;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.entity.enums.NotificationType;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.JobApplicationRepository;
import com.chaineeproject.chainee.repository.JobPostRepository;
import com.chaineeproject.chainee.repository.NotificationRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployerApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 지원 수락: applicant DID + post payment + deadline 반환
     */
    @Transactional
    public ApplicationAcceptResponse acceptApplication(Long applicationId, Long employerUserId) {
        JobApplication app = jobApplicationRepository.findByIdWithPostAndUsers(applicationId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_APPLICATION_NOT_FOUND));

        JobPost post = app.getPost();

        // 권한: 현재 사용자 == 공고 작성자
        if (!post.getAuthor().getId().equals(employerUserId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        // 상태 전이 검증(선택)
        if (app.getStatus() != null && !"pending".equals(app.getStatus())) {
            throw new ApplicationException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // 지원자 DID 검증
        User applicant = app.getApplicant();
        if (applicant.getDid() == null || applicant.getDid().isBlank() || !applicant.isDidVerified()) {
            throw new ApplicationException(ErrorCode.APPLICANT_DID_REQUIRED);
        }

        // 수락 처리
        app.setStatus("accepted");

        return new ApplicationAcceptResponse(
                true,
                app.getStatus(),
                applicant.getDid(),
                post.getPayment(),
                post.getDeadline()
        );
    }

    /**
     * 공고별 지원자 관리 목록
     */
    @Transactional(readOnly = true)
    public PostApplicantsView getApplicantsOfPost(Long employerUserId, Long postId) {
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!post.getAuthor().getId().equals(employerUserId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        List<JobApplication> apps = jobApplicationRepository.findAllByPostIdWithApplicant(postId);

        var items = apps.stream().map(a -> new ApplicantSummary(
                a.getId(),
                a.getApplicant().getId(),
                a.getApplicant().getName(),
                a.getCreatedAt(),
                a.getApplicant().getPositions() == null ? List.of() : a.getApplicant().getPositions()
        )).toList();

        long total = post.getApplicantCount();   // 또는 apps.size() 사용도 가능
        return new PostApplicantsView(post.getId(), post.getTitle(), total, items);
    }

    /**
     * 구인자 전용: 특정 지원서의 이력서를 열람
     */
    @Transactional(readOnly = true)
    public ResumeView getResumeForEmployer(Long employerUserId, Long applicationId) {
        JobApplication app = jobApplicationRepository.findByIdWithPostApplicantAndResume(applicationId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_APPLICATION_NOT_FOUND));

        if (!app.getPost().getAuthor().getId().equals(employerUserId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        Resume r = app.getResume();
        var skills = (r.getSkills() == null || r.getSkills().isBlank())
                ? List.<String>of()
                : java.util.Arrays.stream(r.getSkills().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        return new ResumeView(
                r.getId(),
                r.getTitle(),
                r.getName(),
                r.getIntroduction(),
                r.getDesiredPosition(),
                skills,
                r.getCareerLevel(),
                r.getPortfolioUrl(),
                r.getCreatedAt()
        );
    }

    /**
     * 구인자 → 구직자에게 계약 전자서명 요청 알림 생성
     * - 프론트는 transaction 문자열만 넘긴다.
     * - 백엔드는 transaction을 그대로 Notification에 저장하고,
     *   타이틀/메시지만 사람이 읽을 수 있게 세팅.
     */
    @Transactional
    public ContractNotificationResponse sendContractNotification(
            Long employerId,
            Long applicationId,
            ContractNotificationRequest req
    ) {
        // 1) 지원서 + 연관 엔티티 로딩
        JobApplication app = jobApplicationRepository.findByIdWithPostAndUsers(applicationId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_APPLICATION_NOT_FOUND));

        JobPost post = app.getPost();
        User applicant = app.getApplicant();
        User author = post.getAuthor();

        // 2) 권한 체크: 요청자 == 공고 작성자(구인자)
        if (!author.getId().equals(employerId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        // 3) transaction 필수 체크
        if (req.transaction() == null || req.transaction().isBlank()) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }

        // 4) 알림 내용 구성
        String title = "계약서 전자 서명이 필요합니다";
        String message = String.format(
                "'%s' 공고에 대한 계약서 전자 서명을 진행해 주세요.",
                nz(post.getTitle(), "공고")
        );

        // 5) 알림 생성
        Notification noti = Notification.builder()
                .recipient(applicant) // 알림 받는 사람 = 구직자
                .actor(author)        // 알림을 유발한 사람 = 구인자
                .notificationType(NotificationType.CONTRACT_SIGNATURE_REQUEST)
                .title(title)
                .message(message)
                // linkUrl은 프론트 라우팅 전략에 따라 사용/미사용
                .linkUrl(null)
                .build();

        notificationRepository.save(noti);

        log.debug("Contract signature notification created: notiId={}, applicationId={}",
                noti.getId(), applicationId);

        return new ContractNotificationResponse(
                true,
                "CONTRACT_NOTIFICATION_SENT",
                noti.getId()
        );
    }

    private static String nz(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    /**
     * 🔥 새로 추가: 구직자 → 구인자에게 "서명 완료" 알림 생성
     * - applicantId: JWT uid (요청자 = 구직자)
     * - applicationId: 지원 ID
     * - req.transaction: 서명된 트랜잭션(raw string)
     */
    @Transactional
    public ContractNotificationResponse sendSignedContractNotification(
            Long applicantId,
            Long applicationId,
            ContractNotificationRequest req
    ) {
        JobApplication app = jobApplicationRepository.findByIdWithPostAndUsers(applicationId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_APPLICATION_NOT_FOUND));

        JobPost post = app.getPost();
        User applicant = app.getApplicant();
        User author = post.getAuthor(); // 구인자

        // 권한: 요청자 == 지원자
        if (!applicant.getId().equals(applicantId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        if (req.transaction() == null || req.transaction().isBlank()) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }

        String title = "계약서 전자 서명이 완료되었습니다";
        String message = String.format(
                "%s님이 '%s' 공고에 대한 계약서 전자 서명을 완료했습니다.",
                nz(applicant.getName(), "지원자"),
                nz(post.getTitle(), "공고")
        );

        Notification noti = Notification.builder()
                .recipient(author)    // 알림 받는 사람 = 구인자
                .actor(applicant)     // 알림 유발 = 구직자
                .notificationType(NotificationType.CONTRACT_SIGNED)
                .title(title)
                .message(message)
                .linkUrl(null)
                .build();

        notificationRepository.save(noti);

        log.debug("Contract signed notification created: notiId={}, applicationId={}",
                noti.getId(), applicationId);

        return new ContractNotificationResponse(
                true,
                "CONTRACT_SIGNED_NOTIFICATION_SENT",
                noti.getId()
        );
    }

}
