package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자 프로필 응답 DTO")
public record ProfileResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "이름", example = "여은 김")
        String name,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/images/profile.jpg")
        String profileImageUrl,

        @Schema(description = "포지션 목록", example = "[\"Frontend\", \"UI/UX Designer\"]")
        List<String> positions,

        @Schema(description = "출신 학교", example = "Hongik University")
        String from,

        @Schema(description = "거주 위치", example = "Seoul, Republic of Korea")
        String in,

        @Schema(description = "개인 웹사이트", example = "https://www.yeoeunn.com")
        String website,

        @Schema(description = "소개 헤드라인", example = "Web3에서 협업 혁신을 이끄는 개발자")
        String introductionHeadline,

        @Schema(description = "자기소개 본문", example = "저는 Web3 기반 협업 플랫폼을 개발하며...")
        String introductionContent,

        @Schema(description = "자기소개 이미지 URL", example = "https://cdn.example.com/images/intro.png")
        String introductionPhotoUrl,

        @Schema(description = "대표 프로젝트")
        ProjectDto mainProject,

        @Schema(description = "내 프로젝트 목록")
        List<ProjectDto> myProjects,

        @Schema(description = "작성한 구인글")
        List<JobPostDto> jobPosts,

        @Schema(description = "등록한 이력서 목록")
        List<ResumeDto> resumes
) {}
