package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.FollowActionResponse;
import com.chaineeproject.chainee.dto.PagedUsersResponse;
import com.chaineeproject.chainee.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static com.chaineeproject.chainee.security.SecurityUtils.extractUserIdOrNull;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우/언팔로우 및 목록 API")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    @Operation(summary = "팔로우", description = "타 사용자를 팔로우합니다.")
    public ResponseEntity<FollowActionResponse> follow(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long targetUserId
    ) {
        Long currentUserId = com.chaineeproject.chainee.security.SecurityUtils.uidOrNull(jwt);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(new FollowActionResponse(false, "UNAUTHORIZED"));
        }
        followService.follow(currentUserId, targetUserId);
        return ResponseEntity.ok(new FollowActionResponse(true, "FOLLOWED"));
    }

    @DeleteMapping("/{targetUserId}")
    @Operation(summary = "언팔로우", description = "타 사용자를 언팔로우합니다.")
    public ResponseEntity<FollowActionResponse> unfollow(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long targetUserId
    ) {
        Long currentUserId = com.chaineeproject.chainee.security.SecurityUtils.uidOrNull(jwt);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(new FollowActionResponse(false, "UNAUTHORIZED"));
        }
        followService.unfollow(currentUserId, targetUserId);
        return ResponseEntity.ok(new FollowActionResponse(true, "UNFOLLOWED"));
    }

    @GetMapping("/{userId}/followers")
    @Operation(
            summary = "팔로워 목록",
            description = "해당 사용자를 팔로우하는 사용자 목록을 페이지네이션으로 반환합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedUsersResponse.class))
            )
    )
    public ResponseEntity<PagedUsersResponse> followers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(followService.getFollowers(userId, page, size));
    }

    @GetMapping("/{userId}/following")
    @Operation(
            summary = "팔로잉 목록",
            description = "해당 사용자가 팔로잉하는 사용자 목록을 페이지네이션으로 반환합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedUsersResponse.class))
            )
    )
    public ResponseEntity<PagedUsersResponse> following(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(followService.getFollowing(userId, page, size));
    }
}
