package com.example.order.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ApiError> notFound(OrderNotFoundException e, HttpServletRequest r) { return response(HttpStatus.NOT_FOUND, e.getMessage(), r); }
    @ExceptionHandler(StockUnavailableException.class)
    ResponseEntity<ApiError> conflict(StockUnavailableException e, HttpServletRequest r) { return response(HttpStatus.CONFLICT, e.getMessage(), r); }
    @ExceptionHandler(MedicineNotFoundException.class)
    ResponseEntity<ApiError> medicineNotFound(MedicineNotFoundException e, HttpServletRequest r) { return response(HttpStatus.NOT_FOUND, e.getMessage(), r); }
    @ExceptionHandler(MedicineServiceUnavailableException.class)
    ResponseEntity<ApiError> serviceUnavailable(MedicineServiceUnavailableException e, HttpServletRequest r) { return response(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), r); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> accessDenied(AccessDeniedException e, HttpServletRequest r) { return response(HttpStatus.FORBIDDEN, e.getMessage(), r); }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<ApiError> badRequest(Exception e, HttpServletRequest r) { return response(HttpStatus.BAD_REQUEST, e.getMessage(), r); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> internalError(Exception e, HttpServletRequest r) { return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", r); }
    private ResponseEntity<ApiError> response(HttpStatus s, String m, HttpServletRequest r) { return ResponseEntity.status(s).body(new ApiError(Instant.now(), s.value(), m, r.getRequestURI())); }
    public record ApiError(Instant timestamp, int status, String message, String path) {}
}
