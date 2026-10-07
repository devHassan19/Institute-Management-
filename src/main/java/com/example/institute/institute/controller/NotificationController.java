package com.example.institute.institute.controller;

import com.example.institute.institute.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
public class NotificationController {

    private NotificationService notificationService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return notificationService.subscribe();
    }
    @GetMapping("/test")
    public String testNotification() {

        notificationService.sendNotification(
                "This is a test notification"
        );

        return "Notification sent";
    }
}
