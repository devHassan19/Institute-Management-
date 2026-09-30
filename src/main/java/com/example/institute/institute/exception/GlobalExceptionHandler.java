package com.example.institute.institute.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, String>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());            // 400
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException e) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage());           // 401
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException e) {
        return build(HttpStatus.FORBIDDEN, e.getMessage());              // 403
    }

    @ExceptionHandler(InformationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(InformationNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());              // 404
    }

    @ExceptionHandler(InformationExistException.class)
    public ResponseEntity<Map<String, String>> handleExists(InformationExistException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());               // 409
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<Map<String, String>> handleUnprocessable(UnprocessableEntityException e) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());   // 422
    }

    // JSON خربان أو ناقص أو نوع بيانات غلط (مثل الخطأ اللي طلع لك قبل)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadJson(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "Invalid or malformed request body");   // 400
    }
}