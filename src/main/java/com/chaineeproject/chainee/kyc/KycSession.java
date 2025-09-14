package com.chaineeproject.chainee.kyc;

import com.chaineeproject.chainee.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "kyc_session")
public class KycSession {
    @Id
    @Column(length = 36)
    private String id;               // requestId(UUID)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String phone;
    private String codeHash;         // 6자리 코드 해시 저장
    private LocalDateTime expiresAt;
    private boolean verified;

    @PrePersist
    void pre() { if (this.id == null) this.id = UUID.randomUUID().toString(); }
}
