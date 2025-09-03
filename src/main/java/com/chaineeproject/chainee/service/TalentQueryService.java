// src/main/java/com/chaineeproject/chainee/service/TalentQueryService.java
package com.chaineeproject.chainee.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chaineeproject.chainee.dto.*;
import com.chaineeproject.chainee.entity.Project;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.FollowRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentQueryService {

    /** 정렬 옵션 */
    public enum SortBy { RECENT, NAME, LOCATION, POPULAR }

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ObjectMapper objectMapper;

    /** 탤런트 목록 */
    public PagedTalentsResponse list(Long requesterUserId,
                                     int page, int size,
                                     SortBy sortBy,
                                     boolean includeMainProject) {

        Page<User> p = userRepository.findAll(PageRequest.of(page, size, toSort(sortBy)));

        List<TalentCard> talents = p.getContent().stream()
                .map(u -> toCard(u, requesterUserId, includeMainProject))
                .toList();

        return new PagedTalentsResponse(
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize(), talents
        );
    }

    /** 탤런트 검색 */
    public PagedTalentsResponse search(Long requesterUserId,
                                       String q,
                                       int page, int size,
                                       boolean includeMainProject,
                                       SortBy sortBy) {
        if (q == null || q.isBlank()) {
            throw new IllegalArgumentException("검색어(q)는 비어 있을 수 없습니다.");
        }
        Page<User> p = userRepository.searchTalents(
                q.trim(),
                PageRequest.of(page, size, toSort(sortBy))
        );

        List<TalentCard> talents = p.getContent().stream()
                .map(u -> toCard(u, requesterUserId, includeMainProject))
                .toList();

        return new PagedTalentsResponse(
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize(), talents
        );
    }

    /** 정렬 매핑: Repository에 전달할 Spring Sort */
    private Sort toSort(SortBy sortBy) {
        return switch (sortBy) {
            case RECENT   -> Sort.by(Sort.Direction.DESC, "id");         // 최신 등록 순
            case NAME     -> Sort.by(Sort.Direction.ASC,  "name");       // 이름 오름차순
            case LOCATION -> Sort.by(Sort.Direction.ASC,  "inLocation"); // 지역 오름차순
            case POPULAR  -> Sort.by(Sort.Direction.DESC, "id");         // TODO: followerCount 캐시 후 교체
        };
    }

    /** 카드 매핑 (mainProject 포함 여부 선택) */
    private TalentCard toCard(User u, Long viewerIdOrNull, boolean includeMainProject) {
        Project mp = pickPublicMain(u); // 대표(공개) 우선, 없으면 최근 공개

        TalentProjectPreview preview = (!includeMainProject || mp == null)
                ? null
                : new TalentProjectPreview(
                mp.getId(),
                mp.getTitle(),
                mp.getNftTokenUri(),
                mp.getCompletedAt()
        );

        // 메인 프로젝트의 requiredSkills(JSON) 파싱
        List<String> skills = (mp == null) ? List.of() : parseSkills(mp.getRequiredSkills());

        long followerCount  = followRepository.countByFolloweeId(u.getId());
        long followingCount = followRepository.countByFollowerId(u.getId());
        boolean isFollowing = (viewerIdOrNull != null)
                && !viewerIdOrNull.equals(u.getId())
                && followRepository.existsByFollowerIdAndFolloweeId(viewerIdOrNull, u.getId());

        return new TalentCard(
                u.getId(),
                u.getName(),
                u.getInLocation(),
                u.getProfileImageUrl(),
                u.getPositions(),
                preview,        // includeMainProject=false 이면 null
                skills,
                followerCount,
                followingCount,
                isFollowing
        );
    }

    /** 대표 프로젝트: 설정값이 공개면 우선, 아니면 '가장 최근 공개 프로젝트' */
    private Project pickPublicMain(User u) {
        Project mp = u.getMainProject();
        if (mp != null && mp.isPublic()) return mp;

        return u.getMyProjects().stream()
                .filter(Project::isPublic)
                .sorted(Comparator
                        .comparing(Project::getCompletedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
                )
                .findFirst()
                .orElse(null);
    }

    private List<String> parseSkills(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
