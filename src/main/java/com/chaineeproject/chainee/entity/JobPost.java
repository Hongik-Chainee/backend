package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_post")
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private String title;

    @Lob
    private String description;

    private BigDecimal payment;

    @Lob
    private String requiredSkills;

    private LocalDate deadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}