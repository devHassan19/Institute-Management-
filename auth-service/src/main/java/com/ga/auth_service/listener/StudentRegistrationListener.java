package com.ga.auth_service.listener;

import com.ga.auth_service.event.StudentRegisteredEvent;
import com.ga.auth_service.model.User;
import com.ga.auth_service.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StudentRegistrationListener {

    private final KafkaProducerService kafkaProducerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStudentRegistration(User user) {

        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            return;
        }

        StudentRegisteredEvent event = new StudentRegisteredEvent(
                user.getId(),
                user.getEmailAddress(),
                user.getUsername()
        );

        kafkaProducerService.sendStudentRegisteredEvent(event);
    }
}