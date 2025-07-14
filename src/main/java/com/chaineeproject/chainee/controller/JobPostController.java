package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.JobPostRequest;
import com.chaineeproject.chainee.dto.JobPostResponse;
import com.chaineeproject.chainee.entity.JobPost;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.JobPostRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/job/posts")
@RequiredArgsConstructor
@Slf4j
public class JobPostController {

    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<JobPostResponse> createJobPost(@RequestBody JobPostRequest request) {
        try {
            // 1. 작성자 DID로 유저 조회
            User author = userRepository.findByDid(request.getAuthorDid())
                    .orElseThrow(() -> new IllegalArgumentException("작성자(DID)를 찾을 수 없습니다."));

            // 2. requiredSkills → JSON 문자열로 변환
            String skillsJson = objectMapper.writeValueAsString(request.getRequiredSkills());

            // 3. 엔티티 생성 및 저장
            JobPost post = JobPost.builder()
                    .author(author)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .payment(request.getPayment())
                    .requiredSkills(skillsJson)
                    .duration(request.getDuration())
                    .deadline(request.getDeadline())
                    .createdAt(LocalDateTime.now())
                    .build();

            jobPostRepository.save(post);

            // 4. 응답 반환
            return ResponseEntity.ok(new JobPostResponse(
                    true,
                    "JOB_POST_CREATED",
                    new JobPostResponse.DataWrapper("post-" + post.getId())
            ));

        } catch (JsonProcessingException e) {
            log.error("requiredSkills 변환 실패", e);
            return ResponseEntity.internalServerError().body(new JobPostResponse(
                    false,
                    "SKILL_PARSE_ERROR",
                    null
            ));
        } catch (Exception e) {
            log.error("구인 공고 등록 실패", e);
            return ResponseEntity.internalServerError().body(new JobPostResponse(
                    false,
                    "JOB_POST_CREATION_FAILED",
                    null
            ));
        }
    }
}
