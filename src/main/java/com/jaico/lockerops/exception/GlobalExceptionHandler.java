package com.jaico.lockerops.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ServiceCodeMessageResolver serviceCodeMessageResolver;

    public GlobalExceptionHandler(ServiceCodeMessageResolver serviceCodeMessageResolver) {
        this.serviceCodeMessageResolver = serviceCodeMessageResolver;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors
                                .computeIfAbsent(error.getField(), field -> new ArrayList<>())
                                .add(error.getDefaultMessage())
                );

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.VALIDATION_ERROR.getCode(),
                serviceCodeMessageResolver.resolve(ApiErrorCode.VALIDATION_ERROR.getCode()),
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request) {

        ApiErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = errorCode.getStatus();

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                errorCode.getCode(),
                serviceCodeMessageResolver.resolve(errorCode.getCode(), exception.getArgs()),
                request.getRequestURI(),
                Map.<String, List<String>>of()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.INVALID_REQUEST_BODY.getCode(),
                serviceCodeMessageResolver.resolve(ApiErrorCode.INVALID_REQUEST_BODY.getCode()),
                request.getRequestURI(),
                Map.<String, List<String>>of(
                        "requestBody",
                        List.of("El cuerpo de la petición contiene valores inválidos o con formato incorrecto")
                )
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
