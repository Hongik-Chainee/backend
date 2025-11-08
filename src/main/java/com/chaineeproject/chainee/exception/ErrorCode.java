package com.chaineeproject.chainee.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    APPLICANT_NOT_FOUND(HttpStatus.NOT_FOUND, "지원자 DID에 해당하는 사용자를 찾을 수 없습니다."),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "선택한 이력서를 찾을 수 없습니다."),
    JOB_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 구인 공고를 찾을 수 없습니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 지원서를 찾을 수 없습니다."),
    JOB_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이력서를 찾을 수 없습니다."),

    RESUME_NOT_OWNED_BY_APPLICANT(HttpStatus.FORBIDDEN, "해당 이력서는 지원자 본인의 것이 아닙니다."),
    CANNOT_APPLY_OWN_POST(HttpStatus.FORBIDDEN, "본인이 작성한 공고에는 지원할 수 없습니다."),
    JOB_POST_CLOSED(HttpStatus.BAD_REQUEST, "해당 공고의 마감일이 지났습니다."),
    DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "이미 해당 공고에 지원했습니다."),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    APPLICANT_DID_REQUIRED(HttpStatus.BAD_REQUEST, "지원자의 DID가 등록 또는 검증되지 않았습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "현재 상태에서 요청하신 상태 변경을 수행할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
