// src/main/java/com/chaineeproject/chainee/service/ChatService.java
package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.ChatConversationSummaryDto;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final JobApplicationRepository jobAppRepo;
    private final JobPostRepository jobPostRepo;

    @PersistenceContext
    private EntityManager em;

    /**
     * 공고 작성자(고용주)만 대화를 시작할 수 있음
     */
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

    /**
     * 내가 참여 중인 대화방(고용주 or 지원자) 페이지 조회
     */
    @Transactional(readOnly = true)
    public Page<Conversation> myConversations(Long userId, int page, int size) {
        return conversationRepo.findByPost_Author_IdOrApplicant_Id(userId, userId, PageRequest.of(page, size));
    }

    /**
     * 현재 사용자 기준 대화방 요약 리스트.
     * - 상대방 이름/프로필
     * - 마지막 메시지/시간
     * - 미읽음 카운트
     * 정렬: 간단히 id desc (원하면 updatedAt 컬럼을 Conversation에 추가 후 메시지 전송 때 갱신 권장)
     */
    @Transactional(readOnly = true)
    public List<ChatConversationSummaryDto> listConversationSummaries(Long viewerId) {
        var convs = conversationRepo.findByPost_Author_IdOrApplicant_IdOrderByIdDesc(viewerId, viewerId);

        return convs.stream().map(c -> {
            boolean iAmEmployer = c.getPost().getAuthor().getId().equals(viewerId);
            var partner = iAmEmployer ? c.getApplicant() : c.getPost().getAuthor();

            var last = messageRepo.findTopByConversationIdOrderByCreatedAtDesc(c.getId());
            String lastContent = last != null ? last.getContent() : "";
            var lastAt = last != null ? last.getCreatedAt() : c.getCreatedAt();

            int unread = iAmEmployer
                    ? messageRepo.countByConversationIdAndReadByEmployerFalseAndSender_IdNot(c.getId(), viewerId)
                    : messageRepo.countByConversationIdAndReadByApplicantFalseAndSender_IdNot(c.getId(), viewerId);

            return new ChatConversationSummaryDto(
                    c.getId(),
                    partner.getName(),
                    partner.getProfileImageUrl(),
                    lastContent,
                    lastAt,
                    unread
            );
        }).toList();
    }

    /**
     * 메시지 전송 (참가자만 가능)
     */
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
                .readByApplicant(isApplicant)  // 보낸 쪽을 읽음 처리할지 정책에 맞춰 조정 가능
                .readByEmployer(isEmployer)
                .build();

        var saved = messageRepo.save(msg);

        // (선택) Conversation.updatedAt 갱신 로직을 쓰는 경우 여기서 갱신 권장
        // conv.setUpdatedAt(saved.getCreatedAt());

        return saved;
    }

    /**
     * 과거 메시지 페이지 조회 (참가자만 가능)
     */
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

    /**
     * 읽음 처리 (내가 고용주면 readByEmployer, 지원자면 readByApplicant)
     * 간단 버전: 일괄 로드 후 변경. (규모 커지면 @Modifying bulk update 권장)
     */
    @Transactional
    public void markRead(Long conversationId, Long requesterId) {
        var conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("CONV_NOT_FOUND"));

        boolean isEmployer = conv.getPost().getAuthor().getId().equals(requesterId);
        boolean isApplicant = conv.getApplicant().getId().equals(requesterId);
        if (!isEmployer && !isApplicant) throw new SecurityException("NOT_A_PARTICIPANT");

        messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId, PageRequest.of(0, 1000))
                .forEach(m -> {
                    if (isEmployer) m.setReadByEmployer(true);
                    if (isApplicant) m.setReadByApplicant(true);
                });
    }
}
