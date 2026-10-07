package com.example.institute.institute.controller;

import com.example.institute.institute.model.User;
import com.example.institute.institute.service.EnrollmentService;
import com.example.institute.institute.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class NotificationController {

    private NotificationService notificationService;

    @GetMapping(
            value = "/notifications/subscribe",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribe() {

        User currentUser = EnrollmentService.getCurrentLogginUser();

        return notificationService.subscribe(currentUser.getId());
    }
}