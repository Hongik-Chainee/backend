// src/main/java/com/chaineeproject/chainee/auth/RefreshToken.java
package com.chaineeproject.chainee.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name="refresh_token", indexes = {
        @Index(columnList = "userId"), @Index(columnList = "expiresAt")
})
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    @Column(length = 64, nullable = false) // 토큰 해시(sha256) 저장
    private String tokenHash;
    private Instant issuedAt;
    private Instant expiresAt;
    private boolean revoked;
}
