package com.chaineeproject.chainee.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String did;

    private String provider;
    private String providerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** ===== [프로필 관련 필드] ===== */

    private String profileImageUrl; // 프로필 사진
    private String name;

    @ElementCollection
    @CollectionTable(name = "user_positions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "position_name")
    private List<String> positions = new ArrayList<>();

    @Column(name = "from_univ")
    private String from;

    @Column(name = "in_location")
    private String inLocation;

    private String website;

    private String introductionHeadline;

    @Lob
    private String introductionContent;

    private String introductionPhotoUrl;

    /** ===== [연관 관계] ===== */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobPost> jobPosts = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resume> resumes = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> myProjects = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "main_project_id")
    private Project mainProject;

    @ManyToMany
    @JoinTable(
            name = "user_stored_projects",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private List<Project> storedProjects = new ArrayList<>();

    /** ===== [생성/수정 자동화] ===== */
    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
