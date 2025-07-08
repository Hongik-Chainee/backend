package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vc")
public class VC {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String type;

    @Lob
    private String data;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    private String issuerDid;
}