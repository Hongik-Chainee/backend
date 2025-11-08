package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(name = "ResumeCreateRequest", description = "이력서 생성 요청")
public record ResumeCreateRequest(
        @Schema(description = "이력서 제목", example = "백엔드 개발자 이력서")
        @NotBlank @Size(max = 200)
        String title,

        @Schema(description = "지원자 이름", example = "김여은")
        @NotBlank @Size(max = 100)
        String name,

        @Schema(description = "자기소개", example = "Spring Boot 기반 프로젝트를 다수 진행했습니다.")
        @Size(max = 4000)
        String introduction,

        @Schema(description = "희망 포지션", example = "백엔드 개발자")
        @Size(max = 100)
        String desiredPosition,

        @Schema(description = "보유 기술 목록", example = "[\"Spring Boot\",\"Java\",\"MySQL\"]")
        List<String> skills,

        @Schema(description = "경력 수준", example = "3-5년")
        @Size(max = 50)
        String careerLevel,

        @Schema(description = "포트폴리오 URL", example = "https://portfolio.example.com")
        @Size(max = 500)
        String portfolioUrl
) {}
