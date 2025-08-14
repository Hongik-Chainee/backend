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

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostQueryService {

    private final JobPostRepository jobPostRepository;
    private final FollowRepository followRepository;
    private final ObjectMapper objectMapper;

    public PagedJobPostsResponse list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobPost> p = jobPostRepository.findAll(pageable);

        List<JobPostListItem> items = p.getContent().stream()
                .map(this::toListItem)
                .toList();

        return new PagedJobPostsResponse(
                p.getTotalElements(),
                p.getTotalPages(),
                p.getNumber(),
                p.getSize(),
                items
        );
    }

    public JobPostDetail detail(Long postId) {
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공고를 찾을 수 없습니다. ID=" + postId));
        return toDetail(post);
    }

    // ====== mapping ======
    private JobPostListItem toListItem(JobPost p) {
        User a = p.getAuthor();
        var authorHeader = new ProfileHeaderDto(
                a.getId(),
                a.getName(),
                a.getProfileImageUrl(),
                a.getPositions(),
                a.getFrom(),
                a.getInLocation(),
                a.getWebsite(),
                followRepository.countByFolloweeId(a.getId()),
                followRepository.countByFollowerId(a.getId())
        );
        return new JobPostListItem(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getPayment(),
                parseSkills(p.getRequiredSkills()),
                p.getDuration(),
                p.getDeadline(),
                p.getCreatedAt(),
                p.getApplicantCount(),
                authorHeader
        );
    }

    private JobPostDetail toDetail(JobPost p) {
        User a = p.getAuthor();
        var authorHeader = new ProfileHeaderDto(
                a.getId(),
                a.getName(),
                a.getProfileImageUrl(),
                a.getPositions(),
                a.getFrom(),
                a.getInLocation(),
                a.getWebsite(),
                followRepository.countByFolloweeId(a.getId()),
                followRepository.countByFollowerId(a.getId())
        );
        return new JobPostDetail(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getPayment(),
                parseSkills(p.getRequiredSkills()),
                p.getDuration(),
                p.getDeadline(),
                p.getCreatedAt(),
                p.getApplicantCount(),
                authorHeader
        );
    }

    private List<String> parseSkills(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 파싱 실패 시 빈 리스트
            return Collections.emptyList();
        }
    }
}
