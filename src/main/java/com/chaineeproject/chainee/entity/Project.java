package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String nftTokenUri; // IPFS or NFT 링크

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private boolean isPublic = true;

    @org.hibernate.annotations.CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
