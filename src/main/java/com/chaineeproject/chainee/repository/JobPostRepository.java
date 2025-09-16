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
        select jp
        from JobPost jp
        left join jp.author a
        where
               lower(jp.title) like concat('%', lower(:q), '%')
            or (jp.description is not null    and jp.description    like concat('%', :q, '%'))
            or (jp.requiredSkills is not null and jp.requiredSkills like concat('%', :q, '%'))
            or (a.name is not null            and lower(a.name)     like concat('%', lower(:q), '%'))
        """)
    Page<JobPost> search(@Param("q") String q, Pageable pageable);

    List<JobPost> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
