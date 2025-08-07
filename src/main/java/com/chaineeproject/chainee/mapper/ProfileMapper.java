package com.chaineeproject.chainee.mapper;

import com.chaineeproject.chainee.dto.*;
import com.chaineeproject.chainee.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfileMapper {

    public ProfileResponse toDto(User user, boolean isOwner) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .positions(user.getPositions())
                .from(user.getFrom())
                .in(user.getInLocation())
                .website(user.getWebsite())
                .introductionHeadline(user.getIntroductionHeadline())
                .introductionContent(user.getIntroductionContent())
                .introductionPhotoUrl(user.getIntroductionPhotoUrl())
                .mainProject(toProjectDto(user.getMainProject()))
                .myProjects(user.getMyProjects().stream()
                        .filter(p -> isOwner || !user.getStoredProjects().contains(p))
                        .map(this::toProjectDto)
                        .collect(Collectors.toList()))
                .jobPosts(user.getJobPosts().stream()
                        .map(this::toJobPostDto)
                        .collect(Collectors.toList()))
                .resumes(user.getResumes().stream()
                        .map(this::toResumeDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private ProjectDto toProjectDto(Project p) {
        return ProjectDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .nftTokenUri(p.getNftTokenUri())
                .completedAt(p.getCompletedAt())
                .build();
    }

    private JobPostDto toJobPostDto(JobPost j) {
        return JobPostDto.builder()
                .id(j.getId())
                .title(j.getTitle())
                .description(j.getDescription())
                .payment(j.getPayment())
                .requiredSkills(j.getRequiredSkills())
                .deadline(j.getDeadline())
                .build();
    }

    private ResumeDto toResumeDto(Resume r) {
        return ResumeDto.builder()
                .id(r.getId())
                .title(r.getTitle())
                .name(r.getName())
                .introduction(r.getIntroduction())
                .desiredPosition(r.getDesiredPosition())
                .skills(r.getSkills())
                .careerLevel(r.getCareerLevel())
                .portfolioUrl(r.getPortfolioUrl())
                .build();
    }
}
