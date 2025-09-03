package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "계약 모달용 내 공고 리스트 응답")
public record MyContractPostsResponse(
        @Schema(description = "총 개수", example = "3") int count,
        @Schema(description = "내 공고 목록") List<ContractPostOption> posts
) {}