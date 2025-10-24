// Message.java
package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "message", indexes = {
        @Index(name = "idx_msg_conv_created", columnList = "conversation_id, created_at")
})
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="conversation_id", nullable=false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="sender_id", nullable=false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content; // 텍스트

    private String attachmentUrl; // (옵션) 파일/이미지 업로드 후 URL

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="read_by_applicant", nullable=false)
    private boolean readByApplicant;

    @Column(name="read_by_employer", nullable=false)
    private boolean readByEmployer;

    @PrePersist void onCreate() { this.createdAt = LocalDateTime.now(); }
}
