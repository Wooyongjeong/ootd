package com.github.wooyong.ootd.common;

import org.springframework.http.HttpStatus;

/**
 * Common response codes for API payloads.
 */
public enum ResponseCode {
    OK(HttpStatus.OK, "COMMON-200", "Success"),
    CREATED(HttpStatus.CREATED, "COMMON-201", "Created"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "Bad request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-401", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-403", "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "Not found"),
    CONFLICT(HttpStatus.CONFLICT, "COMMON-409", "Conflict"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON-422", "Validation failed"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "Internal server error");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ResponseCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    /**
     * Map HTTP status to common response code.
     */
    public static ResponseCode fromHttpStatus(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> BAD_REQUEST;
            case UNAUTHORIZED -> UNAUTHORIZED;
            case FORBIDDEN -> FORBIDDEN;
            case NOT_FOUND -> NOT_FOUND;
            case CONFLICT -> CONFLICT;
            case CREATED -> CREATED;
            case OK -> OK;
            default -> INTERNAL_SERVER_ERROR;
        };
    }
}

