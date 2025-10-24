package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Schema(name = "ResumeCreateRequest", description = "이력서 생성 요청")
public record ResumeCreateRequest(
        @Schema(description = "지원자 ID", example = "19")
        @NotBlank Long applicantId,

        @Schema(description = "이력서 제목", example = "프론트엔드 개발자 이력서")
        @NotBlank String title,

        @Schema(description = "지원자 이름", example = "홍길동")
        @NotBlank String name,

        @Schema(description = "자기소개", example = "React와 Next.js 기반 프로젝트를 다수 진행했습니다.")
        String introduction,

        @Schema(description = "희망 포지션", example = "프론트엔드 개발자")
        String desiredPosition,

        @Schema(description = "보유 기술 목록", example = "[\"React\", \"TypeScript\", \"TailwindCSS\"]")
        List<String> skills,

        @Schema(description = "경력 수준", example = "3-5년")
        String careerLevel,

        @Schema(description = "포트폴리오 URL", example = "https://portfolio.example.com")
        String portfolioUrl
) {}
