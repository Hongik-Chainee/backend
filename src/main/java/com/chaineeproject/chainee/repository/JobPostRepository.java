package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.JobPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    @EntityGraph(attributePaths = {"author", "author.positions"})
    Page<JobPost> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "author.positions"})
    Optional<JobPost> findById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JobPost p set p.applicantCount = p.applicantCount + 1 where p.id = :postId")
    int incrementApplicantCount(Long postId);
}
