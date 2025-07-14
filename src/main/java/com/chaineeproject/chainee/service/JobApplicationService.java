package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.JobApplicationRequest;
import com.chaineeproject.chainee.entity.*;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public void applyToJob(Long postId, JobApplicationRequest request) {
        // 1. 지원자 조회
        User applicant = userRepository.findByDid(request.getApplicantDid())
                .orElseThrow(() -> new ApplicationException(ErrorCode.APPLICANT_NOT_FOUND));

        // 2. 공고 조회
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.JOB_POST_NOT_FOUND));

        // 3. 이력서 조회
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESUME_NOT_FOUND));

        // 4. 지원 엔티티 생성 및 저장
        JobApplication application = new JobApplication();
        application.setPost(post);
        application.setApplicant(applicant);
        application.setResume(resume);
        application.setStatus("pending");
        application.setCreatedAt(LocalDateTime.now());

        jobApplicationRepository.save(application);
    }
}
