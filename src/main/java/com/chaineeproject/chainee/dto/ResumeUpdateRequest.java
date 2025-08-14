package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "ResumeUpdateRequest", description = "이력서 수정 요청(부분 업데이트)")
public record ResumeUpdateRequest(
        @Schema(description = "이력서 제목", example = "프론트엔드 개발자 이력서 v2")
        String title,

        @Schema(description = "지원자 이름", example = "홍길동")
        String name,

        @Schema(description = "자기소개", example = "사용자 경험에 집중하는 FE 개발자입니다.")
        String introduction,

        @Schema(description = "희망 포지션", example = "프론트엔드 개발자")
        String desiredPosition,

        @Schema(description = "보유 기술 목록", example = "[\"React\", \"Next.js\", \"TypeScript\"]")
        List<String> skills,

        @Schema(description = "경력 수준", example = "3-5년")
        String careerLevel,

        @Schema(description = "포트폴리오 URL", example = "https://portfolio.example.com")
        String portfolioUrl
) {}
