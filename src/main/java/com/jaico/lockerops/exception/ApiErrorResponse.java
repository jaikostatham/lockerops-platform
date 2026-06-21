package com.jaico.lockerops.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private int code;
    private String message;
    private String path;
    private Map<String, List<String>> fieldErrors;

    public ApiErrorResponse(
            int status,
            String error,
            int code,
            String message,
            String path,
            Map<String, List<String>> fieldErrors
    ) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}
