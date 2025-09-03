// src/main/java/com/chaineeproject/chainee/service/ProfileService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.*;
import com.chaineeproject.chainee.entity.Project;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.exception.ApplicationException;
import com.chaineeproject.chainee.exception.ErrorCode;
import com.chaineeproject.chainee.repository.FollowRepository;
import com.chaineeproject.chainee.repository.ProjectRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FollowRepository followRepository;

    /* ========================= 조회 ========================= */

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long requesterUserIdOrNull, Long profileUserId) {
        User u = userRepository.findById(profileUserId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        boolean isOwner = requesterUserIdOrNull != null && requesterUserIdOrNull.equals(u.getId());

        // 대표 프로젝트(설정값 우선. 타인에게 비공개면 최신 공개 프로젝트로 대체)
        Project effectiveMain = resolveMainProject(u, isOwner);

        // 프로젝트 목록(소유자: 전체, 타인: 공개만), 최신순(desc)
        List<Project> projects = u.getMyProjects().stream()
                .filter(p -> isOwner || Boolean.TRUE.equals(p.isPublic()))
                .sorted(byRecentDesc())
                .toList();

        // 팔로우 정보
        long followerCount  = followRepository.countByFolloweeId(u.getId());
        long followingCount = followRepository.countByFollowerId(u.getId());
        boolean isFollowing = (requesterUserIdOrNull != null)
                && !requesterUserIdOrNull.equals(u.getId())
                && followRepository.existsByFollowerIdAndFolloweeId(requesterUserIdOrNull, u.getId());

        return new ProfileResponse(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getProfileImageUrl(),
                u.getPositions(),
                u.getFrom(),
                u.getInLocation(),
                u.getWebsite(),
                u.getIntroductionHeadline(),
                u.getIntroductionContent(),
                u.getIntroductionPhotoUrl(),
                toProjectDtoOrNull(effectiveMain),
                projects.stream().map(this::toProjectDto).toList(),
                // JobPosts(지원자 수 포함)
                u.getJobPosts().stream().map(j -> new JobPostDto(
                        j.getId(),
                        j.getTitle(),
                        j.getDescription(),
                        j.getPayment(),
                        j.getRequiredSkills(),   // String(JSON) 그대로. 필요하면 List로 바꿔도 됨
                        j.getDeadline(),
                        j.getApplicantCount()
                )).toList(),
                // Resumes
                u.getResumes().stream().map(r -> new ResumeDto(
                        r.getId(),
                        r.getTitle(),
                        r.getName(),
                        r.getIntroduction(),
                        r.getDesiredPosition(),
                        r.getSkills(),
                        r.getCareerLevel(),
                        r.getPortfolioUrl()
                )).toList(),
                // 팔로우 메타
                followerCount,
                followingCount,
                isFollowing
        );
    }

    /* ===================== 프로필 수정 ======================= */

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        if (req.name() != null)                u.setName(req.name());
        if (req.profileImageUrl() != null)     u.setProfileImageUrl(req.profileImageUrl());
        if (req.positions() != null)           u.setPositions(req.positions());
        if (req.from() != null)                u.setFrom(req.from());
        if (req.inLocation() != null)          u.setInLocation(req.inLocation());
        if (req.website() != null)             u.setWebsite(req.website());
        if (req.introductionHeadline() != null)u.setIntroductionHeadline(req.introductionHeadline());
        if (req.introductionContent() != null) u.setIntroductionContent(req.introductionContent());

        // 수정 후 최신 상태 반환(소유자 뷰)
        return getProfile(userId, userId);
    }

    /* =============== 대표 프로젝트 설정/해제 ================= */

    public ProfileResponse updateMainProject(Long userId, MainProjectUpdateRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        if (req.mainProjectId() == null) {
            u.setMainProject(null); // 해제
        } else {
            Project p = projectRepository.findById(req.mainProjectId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_REQUEST)); // PROJECT_NOT_FOUND 없어서 대체

            if (!p.getOwner().getId().equals(u.getId())) {
                throw new ApplicationException(ErrorCode.FORBIDDEN);
            }
            // 대표는 반드시 공개
            if (!p.isPublic()) p.setPublic(true);

            u.setMainProject(p);
        }
        return getProfile(userId, userId);
    }

    /* ======== 단일 프로젝트 공개/비공개 토글(대표 비공개 금지) ======== */

    public ProfileResponse updateProjectVisibility(Long userId, Long projectId, ProjectVisibilityUpdateRequest req) {
        if (req == null || req.isPublic() == null) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_REQUEST)); // PROJECT_NOT_FOUND 대체

        if (!p.getOwner().getId().equals(userId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        boolean wantPublic = req.isPublic();

        // 대표 프로젝트는 비공개 불가
        Project main = u.getMainProject();
        if (!wantPublic && main != null && main.getId().equals(projectId)) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }

        p.setPublic(wantPublic);
        return getProfile(userId, userId);
    }

    /* =============== 마이프로젝트 배치 편집 =================== */
    /**
     * 편집 화면에서 프로젝트별 '메인' / '보관(stored=비공개)' 체크를 받아 한 번에 저장
     * - 메인은 0개 또는 1개만 허용
     * - stored=true => isPublic=false
     * - 메인으로 지정된 프로젝트는 자동으로 공개 처리
     */
    public ProfileResponse batchEditMyProjects(Long userId, ProjectBatchEditRequest req) {
        if (req == null || req.projects() == null || req.projects().isEmpty()) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }

        long mainCount = req.projects().stream().filter(ProjectBatchEditItem::main).count();
        if (mainCount > 1) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST);
        }

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        List<Long> ids = req.projects().stream().map(ProjectBatchEditItem::projectId).toList();
        List<Project> owned = projectRepository.findAllByOwnerIdAndIdIn(userId, ids);

        if (owned.size() != ids.size()) {
            // 소유하지 않은 프로젝트 포함 → FORBIDDEN
            Set<Long> found = owned.stream().map(Project::getId).collect(Collectors.toSet());
            List<Long> notOwned = ids.stream().filter(id -> !found.contains(id)).toList();
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        Map<Long, ProjectBatchEditItem> byId = req.projects().stream()
                .collect(Collectors.toMap(ProjectBatchEditItem::projectId, it -> it));

        // stored 적용 (stored=true ⇒ 비공개)
        for (Project p : owned) {
            boolean stored = Boolean.TRUE.equals(byId.get(p.getId()).stored());
            p.setPublic(!stored);
        }

        // 메인 설정
        if (mainCount == 0) {
            u.setMainProject(null);
        } else {
            Long mainProjectId = req.projects().stream()
                    .filter(ProjectBatchEditItem::main)
                    .findFirst().orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_REQUEST))
                    .projectId();

            Project mainProject = owned.stream()
                    .filter(p -> Objects.equals(p.getId(), mainProjectId))
                    .findFirst()
                    .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_REQUEST));

            if (!mainProject.isPublic()) mainProject.setPublic(true);
            u.setMainProject(mainProject);
        }

        return getProfile(userId, userId);
    }

    /* ====================== 내부 유틸 ======================== */

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

    private Comparator<Project> byRecentDesc() {
        return Comparator
                .comparing(Project::getCompletedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Project::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();
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
