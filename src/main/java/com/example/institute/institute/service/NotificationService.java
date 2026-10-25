package com.example.institute.institute.service;

import com.example.institute.institute.model.Notification;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.repository.NotificationRepository;
import com.example.institute.institute.repository.StudentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               StudentRepository studentRepository) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
    }

    public SseEmitter subscribe(Long userId) {

        System.out.println("CREATING SSE CONNECTION FOR USER: " + userId);

        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(userId, emitter);

        System.out.println("CONNECTED USER: " + userId);
        System.out.println("CONNECTED CLIENTS: " + emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            System.out.println("SSE COMPLETED FOR USER: " + userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId);
            System.out.println("SSE TIMEOUT FOR USER: " + userId);
        });

        emitter.onError(error -> {
            emitters.remove(userId);
            System.out.println("SSE ERROR FOR USER: " + userId);
        });

        return emitter;
    }

    @Async("notificationExecutor")
    public void sendNotification(Long userId, String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] notifying user " + userId);

        // 1) نحفظه في الداتابيس (للطلاب فقط، الأدمن ما عنده Student)
        Student student = studentRepository.findByUser_Id(userId);
        if (student != null) {
            Notification n = new Notification();
            n.setStudent(student);
            n.setType("INFO");
            n.setMessage(message);
            n.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(n);
        }

        // 2) نرسله فوراً لو متصل (SSE)
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            System.out.println("USER " + userId + " IS NOT CONNECTED");
            return;
        }

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("notification")
                            .data(message)
            );

            System.out.println("NOTIFICATION SENT TO USER: " + userId);

        } catch (Exception e) {
            emitters.remove(userId);
            System.out.println("FAILED TO SEND NOTIFICATION TO USER: " + userId);
        }
    }
}