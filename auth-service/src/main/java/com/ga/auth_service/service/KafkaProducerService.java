package com.ga.auth_service.service;

import com.ga.auth_service.event.StudentRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, StudentRegisteredEvent> kafkaTemplate;

    public void sendStudentRegisteredEvent(StudentRegisteredEvent event) {
        kafkaTemplate.send("student-registered", event.userId().toString(), event);
    }
}