package com.github.wooyong.ootd.common;

import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception translator that returns the common API response envelope.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle request-body validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(ResponseCode.VALIDATION_ERROR.httpStatus())
                .body(ApiResponse.error(ResponseCode.VALIDATION_ERROR, ResponseCode.VALIDATION_ERROR.defaultMessage(), errors));
    }

    /**
     * Handle binding errors from query/path parameters.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : exception.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(ResponseCode.VALIDATION_ERROR.httpStatus())
                .body(ApiResponse.error(ResponseCode.VALIDATION_ERROR, ResponseCode.VALIDATION_ERROR.defaultMessage(), errors));
    }

    /**
     * Handle missing required request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception
    ) {
        return ResponseEntity.status(ResponseCode.BAD_REQUEST.httpStatus())
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST, exception.getMessage()));
    }

    /**
     * Handle constraint violations outside request-body binding.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.status(ResponseCode.BAD_REQUEST.httpStatus())
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST, exception.getMessage()));
    }

    /**
     * Handle service-level exceptions that carry an HTTP status.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        ResponseCode responseCode = ResponseCode.fromHttpStatus(status);
        return ResponseEntity.status(responseCode.httpStatus())
                .body(ApiResponse.error(responseCode, exception.getReason() == null ? responseCode.defaultMessage() : exception.getReason()));
    }

    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(ResponseCode.FORBIDDEN.httpStatus())
                .body(ApiResponse.error(ResponseCode.FORBIDDEN, ResponseCode.FORBIDDEN.defaultMessage()));
    }

    /**
     * Handle any uncaught exception.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(ResponseCode.INTERNAL_SERVER_ERROR.httpStatus())
                .body(ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERROR.defaultMessage()));
    }
}

