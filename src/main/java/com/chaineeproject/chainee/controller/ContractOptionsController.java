package com.chaineeproject.chainee.controller;

import com.chaineeproject.chainee.dto.MyContractPostsResponse;
import com.chaineeproject.chainee.service.ContractOptionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts/options")
@RequiredArgsConstructor
@Tag(name = "Contract Options", description = "계약 생성 모달용 옵션 API")
public class ContractOptionsController {

    private final ContractOptionsService contractOptionsService;

    @GetMapping("/my-posts")
    @Operation(
            summary = "계약 모달: 내가 작성한 공고 전체",
            description = "로그인한 사용자가 작성한 모든 공고를 최신순으로 반환합니다(페이징 없음).",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "정상 응답",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MyContractPostsResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "count": 3,
                              "posts": [
                                { "postId": 101, "title": "UI/UX 디자이너 모집", "payment": 5000000, "duration": "5m" },
                                { "postId": 99,  "title": "프론트엔드 개발자 모집", "payment": 7000000, "duration": "6m" },
                                { "postId": 97,  "title": "백엔드 개발자 모집",   "payment": 6000000, "duration": "7m" }
                              ]
                            }
                            """)
                    )
            )
    )
    public ResponseEntity<MyContractPostsResponse> myPostOptions(
            @AuthenticationPrincipal(expression = "id") Long currentUserId
    ) {
        return ResponseEntity.ok(contractOptionsService.myPostOptions(currentUserId));
    }
}
