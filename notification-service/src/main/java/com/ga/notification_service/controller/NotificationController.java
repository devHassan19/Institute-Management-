package com.ga.notification_service.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ConcurrentHashMap<Long, SseEmitter> emitters =
            new ConcurrentHashMap<>();

    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable Long userId) {

        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        return emitter;
    }

    @PostMapping("/send/{userId}")
    public String sendNotification(
            @PathVariable Long userId,
            @RequestBody String message) {

        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            return "User is not connected";
        }

        try {
            emitter.send(message);
            return "Notification sent successfully";
        } catch (IOException e) {
            emitters.remove(userId);
            return "Failed to send notification";
        }
    }
}