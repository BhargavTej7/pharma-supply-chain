package com.example.medicine.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class SecurityErrorHandler {
    @ExceptionHandler(SecurityException.class)
    ResponseEntity<ApiError> securityError(SecurityException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(Instant.now(), 401, exception.getMessage(), request.getRequestURI()));
    }

    record ApiError(Instant timestamp, int status, String message, String path) {}
}
