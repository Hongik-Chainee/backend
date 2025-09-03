package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByPostIdAndApplicantId(Long postId, Long applicantId);
    long countByPostId(Long postId);
    Optional<JobApplication> findWithPostAndUsersById(@Param("id") Long id);
}