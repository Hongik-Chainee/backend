package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이지네이션 사용자 리스트 응답")
public record PagedUsersResponse(
        long totalElements,
        int totalPages,
        int page,
        int size,
        List<UserPreview> users
) {}
