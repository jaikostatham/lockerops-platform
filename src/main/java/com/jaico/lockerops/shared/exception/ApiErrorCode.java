package com.jaico.lockerops.shared.exception;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {

    VALIDATION_ERROR(1000, HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY(1001, HttpStatus.BAD_REQUEST),

    LOCKER_STATION_NOT_FOUND(2001, HttpStatus.NOT_FOUND),

    LOCKER_COMPARTMENT_NOT_FOUND(3001, HttpStatus.NOT_FOUND),
    LOCKER_COMPARTMENT_DUPLICATED_NUMBER(3002, HttpStatus.CONFLICT),

    RESERVATION_NOT_FOUND(4001, HttpStatus.NOT_FOUND),
    LOCKER_COMPARTMENT_NOT_AVAILABLE(4002, HttpStatus.CONFLICT),
    LOCKER_COMPARTMENT_ACTIVE_RESERVATION(4003, HttpStatus.CONFLICT),
    RESERVATION_CANNOT_BE_CANCELLED(4004, HttpStatus.CONFLICT);

    private final int code;
    private final HttpStatus status;

    ApiErrorCode(int code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
