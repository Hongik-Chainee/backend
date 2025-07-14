package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.ResumeRequest;
import com.chaineeproject.chainee.entity.Resume;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.ResumeRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;

    public Long createResume(ResumeRequest request) {
        User applicant = userRepository.findByDid(request.getApplicantDid())
                .orElseThrow(() -> new ApplicationException(ErrorCode.APPLICANT_NOT_FOUND));

        Resume resume = new Resume();
        resume.setOwner(applicant);
        resume.setTitle(request.getTitle());
        resume.setName(request.getName());
        resume.setIntroduction(request.getIntroduction());
        resume.setDesiredPosition(request.getDesiredPosition());
        resume.setSkills(String.join(",", request.getSkills()));
        resume.setCareerLevel(request.getCareerLevel());
        resume.setPortfolioUrl(request.getPortfolioUrl());
        resume.setCreatedAt(LocalDateTime.now());

        return resumeRepository.save(resume).getId();
    }
}
