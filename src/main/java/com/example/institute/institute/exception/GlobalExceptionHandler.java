package com.example.institute.institute.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InformationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleInformationNotFound(
            InformationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)      // 404
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(InformationExistException.class)
    public ResponseEntity<Map<String, String>> handleInformationExist(
            InformationExistException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)       // 409
                .body(Map.of("message", exception.getMessage()));
    }
}