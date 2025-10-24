// JobApplicationRepository.java
package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // (기존) 파서 방식 — 필요 없으면 지워도 됨
    boolean existsByPostIdAndApplicantId(Long postId, Long applicantId);
    long countByPostId(Long postId);

    // ✅ 언더스코어 없이 JPQL로 직접 정의
    @Query("""
        select (count(a) > 0)
        from JobApplication a
        where a.post.id = :postId and a.applicant.id = :applicantId
    """)
    boolean existsForPostAndApplicant(@Param("postId") Long postId,
                                      @Param("applicantId") Long applicantId);

    @Query("""
        select count(a)
        from JobApplication a
        where a.post.id = :postId
    """)
    long countByPost(@Param("postId") Long postId);

    @Query("""
        select a
        from JobApplication a
        where a.post.id = :postId and a.applicant.id = :applicantId
    """)
    Optional<JobApplication> findByPostAndApplicant(@Param("postId") Long postId,
                                                    @Param("applicantId") Long applicantId);

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
