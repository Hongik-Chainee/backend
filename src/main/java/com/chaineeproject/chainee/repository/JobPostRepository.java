package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    // 추후 postId로 검색 등 추가 가능
}
