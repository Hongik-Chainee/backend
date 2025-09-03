// src/main/java/com/chaineeproject/chainee/service/JobPostQueryService.java
package com.chaineeproject.chainee.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chaineeproject.chainee.dto.*;
import com.chaineeproject.chainee.entity.JobPost;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.FollowRepository;
import com.chaineeproject.chainee.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostQueryService {

    public enum SortBy {
        RECENT,            // createdAt DESC
        DEADLINE_ASC,      // deadline ASC
        DEADLINE_DESC,     // deadline DESC
        PAYMENT_ASC,       // payment ASC
        PAYMENT_DESC,      // payment DESC
        APPLICANTS_DESC    // applicantCount DESC
    }

    private final JobPostRepository jobPostRepository;
    private final FollowRepository followRepository;
    private final ObjectMapper objectMapper;

    /** 전체 목록(작성자 필터 없음) */
    public PagedJobPostsResponse list(int page, int size, SortBy sortBy) {
        Page<JobPost> p = jobPostRepository.findAll(PageRequest.of(page, size, toSort(sortBy)));
        return mapToPaged(p);
    }

    /** 검색 */
    public PagedJobPostsResponse search(String q, int page, int size, SortBy sortBy) {
        if (q == null || q.isBlank()) {
            throw new IllegalArgumentException("검색어(q)는 비어 있을 수 없습니다.");
        }
        Page<JobPost> p = jobPostRepository.search(q.trim(), PageRequest.of(page, size, toSort(sortBy)));
        return mapToPaged(p);
    }

    // ===== 내부 유틸 =====
    private Sort toSort(SortBy sortBy) {
        return switch (sortBy) {
            case RECENT         -> Sort.by(Sort.Direction.DESC, "createdAt", "id");
            case DEADLINE_ASC   -> Sort.by(Sort.Direction.ASC,  "deadline").and(Sort.by(Sort.Direction.DESC, "id"));
            case DEADLINE_DESC  -> Sort.by(Sort.Direction.DESC, "deadline").and(Sort.by(Sort.Direction.DESC, "id"));
            case PAYMENT_ASC    -> Sort.by(Sort.Direction.ASC,  "payment").and(Sort.by(Sort.Direction.DESC, "id"));
            case PAYMENT_DESC   -> Sort.by(Sort.Direction.DESC, "payment").and(Sort.by(Sort.Direction.DESC, "id"));
            case APPLICANTS_DESC-> Sort.by(Sort.Direction.DESC, "applicantCount").and(Sort.by(Sort.Direction.DESC, "id"));
        };
    }

    private PagedJobPostsResponse mapToPaged(Page<JobPost> p) {
        var items = p.getContent().stream().map(this::toListItem).toList();
        return new PagedJobPostsResponse(p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize(), items);
    }

    private JobPostListItem toListItem(JobPost j) {
        User a = j.getAuthor();

        var author = new AuthorHeader(
                a.getId(),
                a.getName(),
                a.getProfileImageUrl(),
                a.getPositions(),
                a.getFrom(),      // 필드명이 fromUniv 등으로 변경되어 있다면 그걸 사용
                a.getInLocation(),
                a.getWebsite(),
                followRepository.countByFolloweeId(a.getId()),
                followRepository.countByFollowerId(a.getId())
        );

        return new JobPostListItem(
                j.getId(),
                j.getTitle(),
                j.getDescription(),
                j.getPayment(),
                parseSkills(j.getRequiredSkills()),
                j.getDuration(),
                j.getDeadline(),
                j.getCreatedAt(),
                j.getApplicantCount(),
                author
        );
    }

    private java.util.List<String> parseSkills(String json) {
        if (json == null || json.isBlank()) return java.util.List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<java.util.List<String>>() {});
        } catch (Exception e) {
            return java.util.List.of();
        }
    }
}
