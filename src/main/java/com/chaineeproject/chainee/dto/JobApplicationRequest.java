package com.chaineeproject.chainee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "채용 공고 지원 요청 DTO")
public class JobApplicationRequest {

    @Schema(description = "지원자의 DID (Decentralized Identifier)", example = "did:example:123456789abcdefghi")
    private String applicantDid;

    @Schema(description = "지원자가 선택한 이력서 ID", example = "42")
    private Long resumeId;
}
