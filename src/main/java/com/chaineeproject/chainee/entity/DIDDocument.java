package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "did_document")
public class DIDDocument {
    @Id
    private String did;

    private String publicKey;

    @Lob
    private String serviceEndpoints;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
