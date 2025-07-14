package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "resume")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 예: "프론트엔드_개발자_이력서_2024"
    private String name;
    private String introduction;
    private String desiredPosition;
    private String skills; // JSON 문자열 또는 comma-separated string
    private String careerLevel; // 예: "3년 이상~5년 미만"
    private String portfolioUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    private LocalDateTime createdAt;
}
