// src/main/java/com/chaineeproject/chainee/service/EmployerApplicationService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.ApplicantSummary;
import com.chaineeproject.chainee.dto.ApplicationAcceptResponse;
import com.chaineeproject.chainee.dto.PostApplicantsView;
import com.chaineeproject.chainee.dto.ResumeView;
import com.chaineeproject.chainee.entity.JobApplication;
import com.chaineeproject.chainee.entity.JobPost;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.JobApplicationRepository;
import com.chaineeproject.chainee.repository.JobPostRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;

    /** 지원 수락: applicant DID + post payment + deadline 반환 */
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

    /** 공고별 지원자 관리 목록 */
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

        long total = post.getApplicantCount(); // 실시간이면 apps.size()
        return new PostApplicantsView(post.getId(), post.getTitle(), total, items);
    }

    /** 구인자 전용: 특정 지원서의 이력서를 열람 */
    @Transactional(readOnly = true)
    public ResumeView getResumeForEmployer(Long employerUserId, Long applicationId) {
        var app = jobApplicationRepository.findByIdWithPostApplicantAndResume(applicationId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_APPLICATION_NOT_FOUND));

        if (!app.getPost().getAuthor().getId().equals(employerUserId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        var r = app.getResume();
        var skills = (r.getSkills() == null || r.getSkills().isBlank())
                ? java.util.List.<String>of()
                : java.util.Arrays.stream(r.getSkills().split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();

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
}
