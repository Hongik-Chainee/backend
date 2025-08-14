package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채용 공고 지원 요청 DTO")
public record JobApplicationRequest(
        @Schema(description = "지원자의 DID (Decentralized Identifier)", example = "did:example:123456789abcdefghi")
        String applicantDid,

        @Schema(description = "지원자가 선택한 이력서 ID", example = "42")
        Long resumeId
) {}
