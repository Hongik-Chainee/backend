package com.chaineeproject.chainee.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    APPLICANT_NOT_FOUND("지원자 DID에 해당하는 사용자를 찾을 수 없습니다."),
    RESUME_NOT_FOUND("선택한 이력서를 찾을 수 없습니다."),
    JOB_POST_NOT_FOUND("해당 ID의 구인 공고를 찾을 수 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
