package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc")
public class KYC {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;
    private LocalDate birth;
    private String documentType;
    private String documentNumber;
    private String phoneNumber;
    private String status;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}