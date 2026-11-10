package com.ga.auth_service.listener;

import com.ga.auth_service.model.User;
import com.ga.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StudentRegistrationListener {

    private final UserService userService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStudentRegistration(User user) {
        userService.createStudentInInstitute(user);
    }
}
