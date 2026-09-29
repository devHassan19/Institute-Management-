package com.example.institute.institute.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InformationNotFoundException.class)
    public Map<String, String> handleInformationNotFound(
            InformationNotFoundException exception) {

        return Map.of(
                "message", exception.getMessage()
        );
    }
}