// Conversation.java
package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "conversation",
        uniqueConstraints = @UniqueConstraint(name = "uk_conv_post_applicant",
                columnNames = {"post_id","applicant_id"}))
public class Conversation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 공고에서 시작된 대화인지(구인자 식별을 위해)
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "post_id", nullable = false)
    private JobPost post; // post.getAuthor()가 구인자

    // 구직자
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getEmployerId() { return post.getAuthor().getId(); }
    public Long getApplicantId() { return applicant.getId(); }
}
