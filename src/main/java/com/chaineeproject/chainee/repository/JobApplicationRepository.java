// src/main/java/com/chaineeproject/chainee/repository/JobApplicationRepository.java
package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /**
     * [유지] 서비스에서 사용 중: 중복 지원 방지 체크
     * JobApplicationService.applyToJob() 에서 사용
     */
    boolean existsByPostIdAndApplicantId(Long postId, Long applicantId);

    /**
     * [유지] 공고 지원자 수 조회(집계용)
     * JobPostRepository.incrementApplicantCount(...)와 함께 사용 가능
     */
    long countByPostId(Long postId);

    /**
     * [선택 유지] postId + applicantId로 지원서 단건 조회
     * 필요 시 사용(예: 상태 확인/수정)
     */
    @Query("""
        select a
        from JobApplication a
        where a.post.id = :postId and a.applicant.id = :applicantId
    """)
    Optional<JobApplication> findByPostAndApplicant(@Param("postId") Long postId,
                                                    @Param("applicantId") Long applicantId);

    /**
     * [핵심] 지원 수락 API 용: 지원서 + 공고 + 작성자 + 지원자까지 전부 즉시 로딩
     * EmployerApplicationService.acceptApplication(...) 에서 사용
     */
    @Query("""
        select a
        from JobApplication a
        join fetch a.post p
        join fetch p.author author
        join fetch a.applicant applicant
        where a.id = :id
    """)
    Optional<JobApplication> findByIdWithPostAndUsers(@Param("id") Long id);

    @Query("""
  select a
  from JobApplication a
  join fetch a.applicant u
  join a.post p
  where p.id = :postId
  order by a.createdAt desc, a.id desc
""")
    java.util.List<com.chaineeproject.chainee.entity.JobApplication>
    findAllByPostIdWithApplicant(@Param("postId") Long postId);

    @Query("""
    select a
    from JobApplication a
    join fetch a.post p
    join fetch p.author author
    join fetch a.applicant applicant
    join fetch a.resume r
    where a.id = :id
""")
    Optional<JobApplication> findByIdWithPostApplicantAndResume(@Param("id") Long id);
}
