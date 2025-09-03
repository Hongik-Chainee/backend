package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByPostIdAndApplicantId(Long postId, Long applicantId);
    long countByPostId(Long postId);

    @Query("""
        select a
        from JobApplication a
        join fetch a.post p
        join fetch p.author author
        join fetch a.applicant applicant
        where a.id = :id
    """)
    Optional<JobApplication> findWithPostAndUsersById(@Param("id") Long id);
}