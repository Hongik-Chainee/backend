// src/main/java/com/chaineeproject/chainee/repository/ConversationRepository.java
package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByPost_IdAndApplicant_Id(Long postId, Long applicantId);

    Page<Conversation> findByPost_Author_IdOrApplicant_Id(Long employerId, Long applicantId, Pageable pageable);

    // ✅ 최신순 정렬(없으면 서비스에서 정렬)
    List<Conversation> findByPost_Author_IdOrApplicant_IdOrderByIdDesc(Long employerId, Long applicantId);
}
