package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.ProfileResponse;
import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.mapper.ProfileMapper;
import com.chaineeproject.chainee.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ProfileMapper profileMapper;

    @Operation(
            summary = "사용자 프로필 조회",
            description = """
            해당 userId의 프로필 정보를 반환합니다.
            
            ✅ **본인**일 경우 → 보관된 프로젝트(storedProjects)도 포함되어 반환됩니다.  
            ❌ **타인**일 경우 → 보관된 프로젝트는 포함되지 않습니다.

            --- 

            ### 예시 1: 본인일 때

            ```json
            {
              "id": 1,
              "email": "me@example.com",
              "name": "여은 김",
              "myProjects": [
                { "id": 1, "title": "공개 프로젝트" },
                { "id": 2, "title": "비공개 보관 프로젝트" } // 포함됨
              ]
            }
            ```

            ### 예시 2: 타인일 때

            ```json
            {
              "id": 1,
              "email": "user@example.com",
              "name": "여은 김",
              "myProjects": [
                { "id": 1, "title": "공개 프로젝트" }
              ]
            }
            ```
            """
            ,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "정상 응답",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 사용자",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                        {
                          "status": 404,
                          "message": "사용자를 찾을 수 없습니다."
                        }
                    """)
                            )
                    )
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId
    ) {
        User profileUser = userService.findById(userId);

        boolean isOwner = currentUser != null && currentUser.getId().equals(profileUser.getId());

        ProfileResponse response = profileMapper.toDto(profileUser, isOwner);

        return ResponseEntity.ok(response);
    }
}
