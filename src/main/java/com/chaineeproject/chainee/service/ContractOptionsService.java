package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.ContractPostOption;
import com.chaineeproject.chainee.dto.MyContractPostsResponse;
import com.chaineeproject.chainee.entity.JobPost;
import com.chaineeproject.chainee.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractOptionsService {

    private final JobPostRepository jobPostRepository;

    /** 계약 모달: 내가 작성한 공고 전체(페이징 없음) */
    public MyContractPostsResponse myPostOptions(Long currentUserId) {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        List<JobPost> posts = jobPostRepository.findAllByAuthorIdOrderByCreatedAtDesc(currentUserId);
        List<ContractPostOption> mapped = posts.stream()
                .map(j -> new ContractPostOption(j.getId(), j.getTitle(), j.getPayment(), j.getDuration()))
                .toList();
        return new MyContractPostsResponse(mapped.size(), mapped);
    }
}
