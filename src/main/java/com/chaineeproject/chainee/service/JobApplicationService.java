// src/main/java/com/chaineeproject/chainee/service/JobApplicationService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.entity.*;
import com.chaineeproject.chainee.event.JobApplicationCreatedEvent;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void applyToJob(Long postId, Long applicantId, Long resumeId) {
        // 1) 지원자 조회(JWT uid로 강제)
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.APPLICANT_NOT_FOUND));

        // 2) 공고 조회
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_POST_NOT_FOUND));

        // 3) 이력서 조회 + 소유권 검증(남의 이력서로 지원 방지)
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESUME_NOT_FOUND));
        if (resume.getOwner() == null || !resume.getOwner().getId().equals(applicantId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN); // 또는 전용 에러코드 정의 가능: NOT_OWNER_OF_RESUME
        }

        // 4) 중복 지원 방지
        if (jobApplicationRepository.existsByPostIdAndApplicantId(postId, applicantId)) {
            throw new ApplicationException(ErrorCode.DUPLICATE_APPLICATION);
        }

        // 5) 저장
        JobApplication application = new JobApplication();
        application.setPost(post);
        application.setApplicant(applicant);
        application.setResume(resume);
        application.setStatus("pending");
        application.setCreatedAt(LocalDateTime.now());

        jobApplicationRepository.save(application);
        jobPostRepository.incrementApplicantCount(postId);

        // 6) 이벤트(알림용)
        eventPublisher.publishEvent(new JobApplicationCreatedEvent(application.getId()));
    }
}
