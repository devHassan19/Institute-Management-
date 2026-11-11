package com.ga.auth_service.event;

public record StudentRegisteredEvent(
        Long userId,
        String email,
        String username
) {}