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
    RESERVATION_CANNOT_BE_CANCELLED(4004, HttpStatus.CONFLICT),

    TICKET_NOT_FOUND(5001, HttpStatus.NOT_FOUND),
    TICKET_CODE_NOT_FOUND(5002, HttpStatus.NOT_FOUND),
    TICKET_ALREADY_EXISTS_FOR_RESERVATION(5003, HttpStatus.CONFLICT),
    TICKET_NOT_ACTIVE(5004, HttpStatus.CONFLICT),

    ACCESS_CODE_NOT_FOUND(5101, HttpStatus.NOT_FOUND),
    ACCESS_CODE_EXPIRED(5102, HttpStatus.CONFLICT),
    ACCESS_CODE_REVOKED(5103, HttpStatus.CONFLICT),
    ACCESS_CODE_NOT_ACTIVE(5104, HttpStatus.CONFLICT),
    ACCESS_CODE_RESERVATION_NOT_ACTIVE(5105, HttpStatus.CONFLICT);

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
