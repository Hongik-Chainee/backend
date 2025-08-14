package com.chaineeproject.chainee.service;

import com.chaineeproject.chainee.dto.PagedUsersResponse;
import com.chaineeproject.chainee.dto.UserPreview;
import com.chaineeproject.chainee.entity.Follow;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.FollowRepository;
import com.chaineeproject.chainee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public void follow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }
        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 사용자를 찾을 수 없습니다."));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        if (followRepository.existsByFollowerIdAndFolloweeId(currentUserId, targetUserId)) {
            return; // 이미 팔로우 중이면 무시(idempotent)
        }

        Follow f = Follow.builder()
                .follower(me)
                .followee(target)
                .build();
        followRepository.save(f);
    }

    public void unfollow(Long currentUserId, Long targetUserId) {
        followRepository.deleteByFollowerIdAndFolloweeId(currentUserId, targetUserId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followeeId) {
        if (followerId == null) return false;
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Transactional(readOnly = true)
    public long countFollowers(Long userId) {
        return followRepository.countByFolloweeId(userId);
    }

    @Transactional(readOnly = true)
    public long countFollowing(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Transactional(readOnly = true)
    public PagedUsersResponse getFollowers(Long userId, int page, int size) {
        Page<Follow> p = followRepository.findByFolloweeId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedUsersResponse(
                p.getTotalElements(),
                p.getTotalPages(),
                p.getNumber(),
                p.getSize(),
                p.getContent().stream()
                        .map(f -> toPreview(f.getFollower()))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public PagedUsersResponse getFollowing(Long userId, int page, int size) {
        Page<Follow> p = followRepository.findByFollowerId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedUsersResponse(
                p.getTotalElements(),
                p.getTotalPages(),
                p.getNumber(),
                p.getSize(),
                p.getContent().stream()
                        .map(f -> toPreview(f.getFollowee()))
                        .toList()
        );
    }

    private UserPreview toPreview(User u) {
        return new UserPreview(
                u.getId(),
                u.getName(),
                u.getProfileImageUrl(),
                u.getDid(),
                u.getPositions()
        );
    }
}
