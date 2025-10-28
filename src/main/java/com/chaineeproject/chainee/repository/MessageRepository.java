// src/main/java/com/chaineeproject/chainee/repository/MessageRepository.java
package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    // ✅ 대화방의 마지막 메시지
    Message findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);

    // ✅ 고용주 기준 미읽음 개수 (보낸 사람은 나 제외)
    int countByConversationIdAndReadByEmployerFalseAndSender_IdNot(Long conversationId, Long myId);

    // ✅ 지원자 기준 미읽음 개수 (보낸 사람은 나 제외)
    int countByConversationIdAndReadByApplicantFalseAndSender_IdNot(Long conversationId, Long myId);
}
