package com.chaineeproject.chainee.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    APPLICANT_NOT_FOUND("지원자 DID에 해당하는 사용자를 찾을 수 없습니다."),
    RESUME_NOT_FOUND("선택한 이력서를 찾을 수 없습니다."),
    JOB_POST_NOT_FOUND("해당 ID의 구인 공고를 찾을 수 없습니다."),

    APPLICATION_NOT_FOUND("해당 ID의 지원서를 찾을 수 없습니다."),
    FORBIDDEN("접근 권한이 없습니다."),

    USER_NOT_FOUND("해당 사용자를 찾을 수 없습니다."),
    RESUME_NOT_OWNED_BY_APPLICANT("해당 이력서는 지원자 본인의 것이 아닙니다."),
    JOB_POST_CLOSED("해당 공고의 마감일이 지났습니다."),
    CANNOT_APPLY_OWN_POST("본인이 작성한 공고에는 지원할 수 없습니다."),
    DUPLICATE_APPLICATION("이미 해당 공고에 지원했습니다."),
    INVALID_REQUEST("잘못된 요청입니다.");

    private final String message;
    ErrorCode(String message) {
        this.message = message;
    }
}
