// ConversationRepository.java
package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // (post.id, applicant.id) 조합으로 단일 대화방 조회
    Optional<Conversation> findByPost_IdAndApplicant_Id(Long postId, Long applicantId);

    // 내가 구인자(공고 작성자)거나 구직자인 대화방 목록
    Page<Conversation> findByPost_Author_IdOrApplicant_Id(Long employerId, Long applicantId, Pageable pageable);
}
