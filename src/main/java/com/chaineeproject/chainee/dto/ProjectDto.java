package com.chaineeproject.chainee.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectDto {
    private Long id;
    private String title;
    private String nftTokenUri;
    private LocalDateTime completedAt;
    private Boolean isPublic;
}
