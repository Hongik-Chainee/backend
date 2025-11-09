// src/main/java/com/chaineeproject/chainee/service/JobApplicationService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.entity.*;
import com.chaineeproject.chainee.event.JobApplicationCreatedEvent;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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
        jobPostRepository.incrementApplicantCount(postId);

        log.debug("Publishing JobApplicationCreatedEvent: {}", application.getId());
        eventPublisher.publishEvent(new JobApplicationCreatedEvent(application.getId()));
    }
}
