package com.ga.notification_service.consumer;

import com.ga.notification_service.event.StudentRegisteredEvent;
import com.ga.notification_service.controller.NotificationController;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class StudentRegisteredConsumer {

    private final NotificationController notificationController;

    @KafkaListener(
            topics = "student-registered",
            groupId = "notification-group"
    )
    public void consume(StudentRegisteredEvent event) {

        System.out.println("Notification received!");
        System.out.println("Student: " + event.username());
        System.out.println("Email: " + event.email());

        notificationController.sendNotification(
                event.userId(),
                "Welcome " + event.username() + "! Your registration is successful."
        );
    }
}