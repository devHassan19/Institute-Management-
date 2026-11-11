package com.example.institute.institute.event;

public record StudentRegisteredEvent(
        Long userId,
        String email,
        String username
) {}