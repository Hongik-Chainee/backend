package com.chaineeproject.chainee.mapper;

import com.chaineeproject.chainee.dto.*;
import com.chaineeproject.chainee.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfileMapper {

    public ProfileResponse toDto(User user, boolean isOwner) {
        Project main = resolveMainProject(user, isOwner);

        List<ProjectDto> myProjects = user.getMyProjects().stream()
                .filter(p -> isOwner || Boolean.TRUE.equals(p.isPublic()))
                .sorted(this::compareByRecent) // 최신 우선
                .map(this::toProjectDto)
                .collect(Collectors.toList());

        List<JobPostDto> jobPosts = user.getJobPosts().stream()
                .map(this::toJobPostDto)
                .collect(Collectors.toList());

        List<ResumeDto> resumes = user.getResumes().stream()
                .map(this::toResumeDto)
                .collect(Collectors.toList());

        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                user.getPositions(),
                user.getFrom(),
                user.getInLocation(),
                user.getWebsite(),
                user.getIntroductionHeadline(),
                user.getIntroductionContent(),
                user.getIntroductionPhotoUrl(),
                toProjectDtoOrNull(main),
                myProjects,
                jobPosts,
                resumes
        );
    }

    private Project resolveMainProject(User user, boolean isOwner) {
        Project mp = user.getMainProject();
        if (mp != null) {
            if (isOwner || Boolean.TRUE.equals(mp.isPublic())) return mp; // 타인에게는 공개일 때만
        }
        // 대체 후보: 최신(완료일 → 생성시각) & 타인에게는 공개만
        return user.getMyProjects().stream()
                .filter(p -> isOwner || Boolean.TRUE.equals(p.isPublic()))
                .sorted(this::compareByRecent) // 최근 우선
                .findFirst()
                .orElse(null);
    }

    private int compareByRecent(Project a, Project b) {
        // completedAt 우선, 같거나 null이면 createdAt 비교
        int c = nullsLastCompare(a.getCompletedAt(), b.getCompletedAt());
        if (c != 0) return -c; // desc
        return -nullsLastCompare(a.getCreatedAt(), b.getCreatedAt()); // desc
    }

    private <T extends Comparable<T>> int nullsLastCompare(T x, T y) {
        if (x == null && y == null) return 0;
        if (x == null) return -1;
        if (y == null) return 1;
        return x.compareTo(y);
    }

    private ProjectDto toProjectDtoOrNull(Project p) {
        return (p == null) ? null : toProjectDto(p);
    }

    private ProjectDto toProjectDto(Project p) {
        return new ProjectDto(
                p.getId(),
                p.getTitle(),
                p.getNftTokenUri(),
                p.getCompletedAt(),
                p.isPublic()
        );
    }

    private JobPostDto toJobPostDto(JobPost j) {
        // JobPostDto가 record로 바뀌었으니 생성자 호출
        return new JobPostDto(
                j.getId(),
                j.getTitle(),
                j.getDescription(),
                j.getPayment(),
                j.getRequiredSkills(),
                j.getDeadline()
        );
    }

    private ResumeDto toResumeDto(Resume r) {
        return new ResumeDto(
                r.getId(),
                r.getTitle(),
                r.getName(),
                r.getIntroduction(),
                r.getDesiredPosition(),
                r.getSkills(),
                r.getCareerLevel(),
                r.getPortfolioUrl()
        );
    }
}
