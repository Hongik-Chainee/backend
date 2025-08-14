package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.JobPostDto;
import com.chaineeproject.chainee.dto.MainProjectUpdateRequest;
import com.chaineeproject.chainee.dto.ProfileResponse;
import com.chaineeproject.chainee.dto.ProfileUpdateRequest;
import com.chaineeproject.chainee.dto.ProjectBatchEditItem;
import com.chaineeproject.chainee.dto.ProjectBatchEditRequest;
import com.chaineeproject.chainee.dto.ProjectDto;
import com.chaineeproject.chainee.dto.ResumeDto;
import com.chaineeproject.chainee.entity.Project;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.FollowRepository;
import com.chaineeproject.chainee.repository.ProjectRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 요구사항:
 * - 대표 프로젝트: 지정값 우선, 없거나(또는 타인에게 비공개인 경우) 최신(완료일 -> 생성일) 프로젝트로 fallback
 * - 공개/비공개: Project.isPublic (기본 true)
 * - 배치 편집: 프로젝트별 [메인]/[보관(stored=비공개)] 체크 후 한 번에 저장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FollowRepository followRepository;

    // ========================= 조회 =========================
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long requesterUserIdOrNull, Long profileUserId) {
        User profileUser = userRepository.findById(profileUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + profileUserId));

        boolean isOwner = requesterUserIdOrNull != null && requesterUserIdOrNull.equals(profileUser.getId());

        // 대표 프로젝트(설정값 우선, 타인에게 비공개면 최신 공개 프로젝트로 대체)
        Project effectiveMain = resolveMainProject(profileUser, isOwner);

        // 프로젝트 목록(소유자: 전체, 타인: 공개만), 최신순 정렬
        List<Project> projects = profileUser.getMyProjects().stream()
                .filter(p -> isOwner || Boolean.TRUE.equals(p.isPublic()))
                .sorted(byRecentDesc())
                .toList();

        // record 생성자 기반 매핑
        List<ProjectDto> myProjects = projects.stream()
                .map(this::toProjectDto)
                .toList();

        List<JobPostDto> jobPosts = profileUser.getJobPosts().stream()
                .map(p -> new JobPostDto(
                        p.getId(),
                        p.getTitle(),
                        p.getDescription(),
                        p.getPayment(),
                        p.getRequiredSkills(),
                        p.getDeadline()
                ))
                .toList();

        List<ResumeDto> resumes = profileUser.getResumes().stream()
                .map(r -> new ResumeDto(
                        r.getId(),
                        r.getTitle(),
                        r.getName(),
                        r.getIntroduction(),
                        r.getDesiredPosition(),
                        r.getSkills(),
                        r.getCareerLevel(),
                        r.getPortfolioUrl()
                ))
                .toList();

        long followerCount = followRepository.countByFolloweeId(profileUserId);
        long followingCount = followRepository.countByFollowerId(profileUserId);
        boolean isFollowing = (requesterUserIdOrNull != null)
                && followRepository.existsByFollowerIdAndFolloweeId(requesterUserIdOrNull, profileUserId);


        return new ProfileResponse(
                profileUser.getId(),
                profileUser.getEmail(),
                profileUser.getName(),
                profileUser.getProfileImageUrl(),
                profileUser.getPositions(),
                profileUser.getFrom(),
                profileUser.getInLocation(),
                profileUser.getWebsite(),
                profileUser.getIntroductionHeadline(),
                profileUser.getIntroductionContent(),
                profileUser.getIntroductionPhotoUrl(),
                toProjectDtoOrNull(effectiveMain),
                myProjects,
                jobPosts,
                resumes,
                followerCount,
                followingCount,
                isFollowing
        );
    }

    // ===================== 프로필 수정 =======================
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        if (req.name() != null) u.setName(req.name());
        if (req.profileImageUrl() != null) u.setProfileImageUrl(req.profileImageUrl());
        if (req.positions() != null) u.setPositions(req.positions());
        if (req.from() != null) u.setFrom(req.from());
        if (req.inLocation() != null) u.setInLocation(req.inLocation());
        if (req.website() != null) u.setWebsite(req.website());
        if (req.introductionHeadline() != null) u.setIntroductionHeadline(req.introductionHeadline());
        if (req.introductionContent() != null) u.setIntroductionContent(req.introductionContent());

        // 수정 후 최신 상태 반환(소유자 뷰 규칙)
        return getProfile(userId, userId);
    }

    // =============== 대표 프로젝트 설정/해제 =================
    public ProfileResponse updateMainProject(Long userId, MainProjectUpdateRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        if (req.mainProjectId() == null) {
            u.setMainProject(null); // 해제
        } else {
            Project p = projectRepository.findById(req.mainProjectId())
                    .orElseThrow(() -> new IllegalArgumentException("프로젝트가 없습니다. ID: " + req.mainProjectId()));
            if (!p.getOwner().getId().equals(u.getId())) {
                throw new IllegalStateException("본인 프로젝트만 대표로 설정할 수 있습니다.");
            }
            // 대표는 항상 공개로 강제
            if (!p.isPublic()) p.setPublic(true);
            u.setMainProject(p);
        }
        return getProfile(userId, userId);
    }

    // =============== 프로젝트 공개/비공개 변경 =================
    public void updateProjectVisibility(Long userId, Long projectId, boolean isPublic) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 없습니다. ID: " + projectId));
        if (!p.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("본인 프로젝트만 공개 범위를 변경할 수 있습니다.");
        }
        p.setPublic(isPublic);
    }

    // =============== 마이프로젝트 배치 편집 ===================
    /**
     * 편집 화면에서 프로젝트별 '메인' / '보관(stored=비공개)' 체크를 받아 한 번에 저장
     * - 메인은 0개 또는 1개만 허용
     * - stored=true => isPublic=false
     * - 메인으로 지정된 프로젝트는 자동으로 공개 처리
     */
    public ProfileResponse batchEditMyProjects(Long userId, ProjectBatchEditRequest req) {
        if (req == null || req.projects() == null || req.projects().isEmpty()) {
            throw new IllegalArgumentException("수정할 프로젝트 목록이 비어있습니다.");
        }

        long mainCount = req.projects().stream().filter(ProjectBatchEditItem::main).count();
        if (mainCount > 1) {
            throw new IllegalArgumentException("메인 프로젝트는 하나만 선택할 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID=" + userId));

        List<Long> ids = req.projects().stream().map(ProjectBatchEditItem::projectId).toList();
        List<Project> owned = projectRepository.findAllByOwnerIdAndIdIn(userId, ids);

        if (owned.size() != ids.size()) {
            Set<Long> foundIds = owned.stream().map(Project::getId).collect(Collectors.toSet());
            List<Long> notOwned = ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new IllegalStateException("본인 소유가 아닌 프로젝트가 포함되어 있습니다: " + notOwned);
        }

        Map<Long, ProjectBatchEditItem> byId = req.projects().stream()
                .collect(Collectors.toMap(ProjectBatchEditItem::projectId, it -> it));

        // 공개/비공개 적용 (stored=true => isPublic=false)
        for (Project p : owned) {
            boolean stored = byId.get(p.getId()).stored();
            p.setPublic(!stored);
        }

        // 메인 설정 적용
        if (mainCount == 0) {
            user.setMainProject(null); // 해제
        } else {
            Long mainProjectId = req.projects().stream()
                    .filter(ProjectBatchEditItem::main)
                    .findFirst().orElseThrow().projectId();

            Project mainProject = owned.stream()
                    .filter(p -> Objects.equals(p.getId(), mainProjectId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("메인으로 지정한 프로젝트를 찾을 수 없습니다."));

            if (!mainProject.isPublic()) mainProject.setPublic(true);
            user.setMainProject(mainProject);
        }

        return getProfile(userId, userId);
    }

    // ====================== 내부 유틸 ========================
    private Project resolveMainProject(User profileUser, boolean isOwner) {
        Project mp = profileUser.getMainProject();
        if (mp != null && (isOwner || Boolean.TRUE.equals(mp.isPublic()))) {
            return mp;
        }
        return profileUser.getMyProjects().stream()
                .filter(p -> isOwner || Boolean.TRUE.equals(p.isPublic()))
                .sorted(byRecentDesc())
                .findFirst()
                .orElse(null);
    }

    private java.util.Comparator<Project> byRecentDesc() {
        return Comparator
                .comparing(Project::getCompletedAt, nullsLast())
                .thenComparing(Project::getCreatedAt, nullsLast())
                .reversed();
    }

    private static <T extends Comparable<T>> Comparator<T> nullsLast() {
        return Comparator.nullsLast(Comparator.naturalOrder());
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

    private ProjectDto toProjectDtoOrNull(Project p) {
        return (p == null) ? null : toProjectDto(p);
    }
}
