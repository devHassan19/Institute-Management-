package com.example.institute.institute.controller;

import com.example.institute.institute.model.Notification;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.NotificationRepository;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.service.EnrollmentService;
import com.example.institute.institute.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class NotificationController {

    private NotificationService notificationService;
    private StudentRepository studentRepository;
    private NotificationRepository notificationRepository;

    @GetMapping(value = "/notifications/subscribe",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribe() {
        User currentUser = EnrollmentService.getCurrentLogginUser();
        return notificationService.subscribe(currentUser.getId());
    }

    @GetMapping("/notifications/list")
    public List<Notification> myNotifications() {
        User currentUser = EnrollmentService.getCurrentLogginUser();
        Student student = studentRepository.findByUser_Id(currentUser.getId());
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
    }
}