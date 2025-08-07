package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "사용자 프로필 응답 DTO")
public class ProfileResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "여은 김")
    private String name;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/images/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "포지션 목록", example = "[\"Frontend\", \"UI/UX Designer\"]")
    private List<String> positions;

    @Schema(description = "출신 학교", example = "Hongik University")
    private String from;

    @Schema(description = "거주 위치", example = "Seoul, Republic of Korea")
    private String in;

    @Schema(description = "개인 웹사이트", example = "https://www.yeoeunn.com")
    private String website;

    @Schema(description = "소개 헤드라인", example = "Web3에서 협업 혁신을 이끄는 개발자")
    private String introductionHeadline;

    @Schema(description = "자기소개 본문", example = "저는 Web3 기반 협업 플랫폼을 개발하며...")
    private String introductionContent;

    @Schema(description = "자기소개 이미지 URL", example = "https://cdn.example.com/images/intro.png")
    private String introductionPhotoUrl;

    @Schema(description = "대표 프로젝트")
    private ProjectDto mainProject;

    @Schema(description = "내 프로젝트 목록")
    private List<ProjectDto> myProjects;

    @Schema(description = "작성한 구인글")
    private List<JobPostDto> jobPosts;

    @Schema(description = "등록한 이력서 목록")
    private List<ResumeDto> resumes;
}
