package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.JobPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    @EntityGraph(attributePaths = {"author", "author.positions"})
    Page<JobPost> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "author.positions"})
    Optional<JobPost> findById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JobPost p set p.applicantCount = p.applicantCount + 1 where p.id = :postId")
    int incrementApplicantCount(Long postId);

    @Query("""
        select p from JobPost p
        left join p.author a
        where
            lower(p.title) like lower(concat('%', :q, '%'))
         or lower(p.description) like lower(concat('%', :q, '%'))
         or lower(a.name) like lower(concat('%', :q, '%'))
         or p.requiredSkills like concat('%', :q, '%')
        """)
    Page<JobPost> search(@Param("q") String q, Pageable pageable);

    List<JobPost> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
