package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "이력서 등록 요청 DTO")
public class ResumeRequest {

    @Schema(description = "지원자 DID", example = "did:example:xyz789")
    private String applicantDid;

    @Schema(description = "이력서 제목", example = "프론트엔드 개발자 이력서")
    private String title;

    @Schema(description = "지원자 이름", example = "홍길동")
    private String name;

    @Schema(description = "자기소개", example = "React와 Next.js 기반 프로젝트를 다수 진행했습니다.")
    private String introduction;

    @Schema(description = "희망 포지션", example = "프론트엔드 개발자")
    private String desiredPosition;

    @Schema(description = "보유 기술 목록", example = "[\"React\", \"TypeScript\", \"TailwindCSS\"]")
    private List<String> skills;

    @Schema(description = "경력 수준", example = "3-5년")
    private String careerLevel;

    @Schema(description = "포트폴리오 URL", example = "https://portfolio.example.com")
    private String portfolioUrl;
}
