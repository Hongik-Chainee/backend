// ChatService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.entity.Conversation;
import com.chaineeproject.chainee.entity.Message;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.ConversationRepository;
import com.chaineeproject.chainee.repository.JobApplicationRepository;
import com.chaineeproject.chainee.repository.JobPostRepository;
import com.chaineeproject.chainee.repository.MessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final JobApplicationRepository jobAppRepo;
    private final JobPostRepository jobPostRepo;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Conversation startConversation(Long postId, Long applicantId, Long requesterId) {
        // 중복 생성 방지
        var existing = conversationRepo.findByPost_IdAndApplicant_Id(postId, applicantId);
        if (existing.isPresent()) return existing.get();

        var post = jobPostRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("POST_NOT_FOUND"));

        // 권한: 대화 시작은 공고 작성자만
        if (!post.getAuthor().getId().equals(requesterId)) {
            throw new SecurityException("ONLY_EMPLOYER_CAN_START");
        }

        // 지원 여부 검증
        boolean hasApplied = jobAppRepo.existsForPostAndApplicant(postId, applicantId);
        if (!hasApplied) throw new IllegalStateException("APPLICANT_NOT_APPLIED");

        // applicant 프록시 참조(TransientProperty 방지)
        var applicantRef = em.getReference(User.class, applicantId);

        var conv = Conversation.builder()
                .post(post)
                .applicant(applicantRef)
                .build();

        return conversationRepo.save(conv);
    }

    @Transactional(readOnly = true)
    public Page<Conversation> myConversations(Long userId, int page, int size) {
        // 구인자(작성자)거나 구직자인 대화방을 한 번에 조회
        return conversationRepo.findByPost_Author_IdOrApplicant_Id(userId, userId, PageRequest.of(page, size));
    }

    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, String content, String attachmentUrl) {
        var conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("CONV_NOT_FOUND"));

        // 참가자 검증
        boolean isEmployer = conv.getPost().getAuthor().getId().equals(senderId);
        boolean isApplicant = conv.getApplicant().getId().equals(senderId);
        if (!isEmployer && !isApplicant) throw new SecurityException("NOT_A_PARTICIPANT");

        var senderRef = em.getReference(User.class, senderId);

        var msg = Message.builder()
                .conversation(conv)
                .sender(senderRef)
                .content(content)
                .attachmentUrl(attachmentUrl)
                .readByApplicant(isApplicant)  // 보낸 사람 쪽은 읽음으로 둘지 여부는 정책에 맞춰
                .readByEmployer(isEmployer)
                .build();

        return messageRepo.save(msg);
    }

    @Transactional(readOnly = true)
    public Page<Message> getMessages(Long conversationId, Long requesterId, int page, int size) {
        var conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("CONV_NOT_FOUND"));

        if (!conv.getPost().getAuthor().getId().equals(requesterId) &&
                !conv.getApplicant().getId().equals(requesterId)) {
            throw new SecurityException("NOT_A_PARTICIPANT");
        }

        return messageRepo.findByConversationIdOrderByCreatedAtAsc(
                conversationId, PageRequest.of(page, size)
        );
    }

    @Transactional
    public void markRead(Long conversationId, Long requesterId) {
        var conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("CONV_NOT_FOUND"));

        boolean isEmployer = conv.getPost().getAuthor().getId().equals(requesterId);
        boolean isApplicant = conv.getApplicant().getId().equals(requesterId);
        if (!isEmployer && !isApplicant) throw new SecurityException("NOT_A_PARTICIPANT");

        // 간단 버전: 일괄 로드 후 플래그 변경 (규모 커지면 @Modifying bulk-update 권장)
        messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId, PageRequest.of(0, 1000))
                .forEach(m -> {
                    if (isEmployer) m.setReadByEmployer(true);
                    if (isApplicant) m.setReadByApplicant(true);
                });
    }
}
