package com.jaico.lockerops.exception;

public class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;
    private final Object[] args;

    public ApiException(ApiErrorCode errorCode, Object... args) {
        super("Service error code: " + errorCode.getCode());
        this.errorCode = errorCode;
        this.args = args;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }
}
