package com.ga.notification_service.event;

public record StudentRegisteredEvent(
        Long userId,
        String email,
        String username
) {}