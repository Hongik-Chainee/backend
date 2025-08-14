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
                .mainProject(toProjectDtoOrNull(main))
                // ✅ 공개/비공개 필터: 소유자는 전체, 타인은 공개만
                .myProjects(user.getMyProjects().stream()
                        .filter(p -> isOwner || p.isPublic())
                        .sorted((a, b) -> compareByRecent(b, a)) // 최신 우선
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

    private Project resolveMainProject(User user, boolean isOwner) {
        Project mp = user.getMainProject();
        if (mp != null) {
            if (isOwner || mp.isPublic()) return mp; // 타인에게는 공개일 때만
        }
        // 대체 후보: 최신(완료일 → 생성시각) & 타인에게는 공개만
        return user.getMyProjects().stream()
                .filter(p -> isOwner || p.isPublic())
                .sorted(this::compareByRecent) // 최근 우선
                .findFirst()
                .orElse(null);
    }

    private int compareByRecent(Project a, Project b) {
        // completedAt 우선 정렬, 같거나 null이면 createdAt으로
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
