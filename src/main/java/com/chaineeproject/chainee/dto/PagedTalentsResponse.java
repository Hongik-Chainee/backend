package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "탤런트 목록 페이지네이션 응답")
public record PagedTalentsResponse(
        long totalElements,
        int totalPages,
        int page,
        int size,
        List<TalentCard> talents
) {}