package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.*;
import com.chaineeproject.chainee.entity.Resume;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.ResumeRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResumeService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;

    // ========= 생성 =========
    public Long createResume(ResumeCreateRequest request) {
        User applicant = userRepository.findById(request.applicantId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.APPLICANT_NOT_FOUND));

        Resume resume = new Resume();
        resume.setOwner(applicant);
        resume.setTitle(request.title());
        resume.setName(request.name());
        resume.setIntroduction(request.introduction());
        resume.setDesiredPosition(request.desiredPosition());
        if (request.skills() != null) resume.setSkills(String.join(",", request.skills()));
        resume.setCareerLevel(request.careerLevel());
        resume.setPortfolioUrl(request.portfolioUrl());
        resume.setCreatedAt(LocalDateTime.now());

        return resumeRepository.save(resume).getId();
    }

    // ========= 조회(본인) 목록 =========
    @Transactional(readOnly = true)
    public List<ResumeView> getMyResumes(Long currentUserId) {
        return resumeRepository.findAllByOwnerId(currentUserId)
                .stream().map(this::toView).toList();
    }

    // ========= 조회(본인) 단건 =========
    @Transactional(readOnly = true)
    public ResumeView getMyResume(Long currentUserId, Long resumeId) {
        Resume r = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESUME_NOT_FOUND));
        if (!r.getOwner().getId().equals(currentUserId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }
        return toView(r);
    }

    // ========= 수정(본인, 부분 업데이트) =========
    public ResumeView updateMyResume(Long currentUserId, Long resumeId, ResumeUpdateRequest req) {
        Resume r = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESUME_NOT_FOUND));
        if (!r.getOwner().getId().equals(currentUserId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        if (req.title() != null) r.setTitle(req.title());
        if (req.name() != null) r.setName(req.name());
        if (req.introduction() != null) r.setIntroduction(req.introduction());
        if (req.desiredPosition() != null) r.setDesiredPosition(req.desiredPosition());
        if (req.skills() != null) r.setSkills(String.join(",", req.skills()));
        if (req.careerLevel() != null) r.setCareerLevel(req.careerLevel());
        if (req.portfolioUrl() != null) r.setPortfolioUrl(req.portfolioUrl());

        return toView(r);
    }

    // ========= 매핑 유틸 =========
    private ResumeView toView(Resume r) {
        List<String> skillsList =
                (r.getSkills() == null || r.getSkills().isBlank())
                        ? List.of()
                        : Arrays.stream(r.getSkills().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList();

        return new ResumeView(
                r.getId(),
                r.getTitle(),
                r.getName(),
                r.getIntroduction(),
                r.getDesiredPosition(),
                skillsList,
                r.getCareerLevel(),
                r.getPortfolioUrl(),
                r.getCreatedAt()
        );
    }
}
