// src/main/java/com/chaineeproject/chainee/dto/AuthorHeader.java
package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "작성자 요약 정보")
public record AuthorHeader(
        @Schema(description = "작성자 ID") Long id,
        @Schema(description = "작성자 이름") String name,
        @Schema(description = "프로필 이미지 URL") String profileImageUrl,
        @Schema(description = "포지션(joined)") List<String> positions,
        @Schema(description = "학교/소속 등 from 필드") String from,
        @Schema(description = "거주/위치 inLocation") String inLocation,
        @Schema(description = "웹사이트") String website,
        @Schema(description = "팔로워 수") long followers,
        @Schema(description = "팔로잉 수") long followings
) {}
