package com.chaineeproject.chainee.repository;

import com.chaineeproject.chainee.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    long countByFolloweeId(Long followeeId); // 그 사람을 팔로우하는 사람 수 = followers
    long countByFollowerId(Long followerId); // 그 사람이 팔로잉하는 수 = following

    Page<Follow> findByFolloweeId(Long followeeId, Pageable pageable); // followers 목록
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable); // following 목록

    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
}
