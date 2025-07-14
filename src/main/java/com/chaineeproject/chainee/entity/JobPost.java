package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Column(name = "required_skills")
    private String requiredSkills; // JSON 문자열로 저장

    private String duration; // "6m", "1y" 등

    private LocalDate deadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
