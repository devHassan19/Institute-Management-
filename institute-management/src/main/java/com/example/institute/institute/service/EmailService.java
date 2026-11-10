package com.example.institute.institute.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Verify your account");
        msg.setText("Click the link to verify your account:\n"
                + baseUrl + "/auth/users/verify?token=" + token);
        mailSender.send(msg);
    }

    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Reset your password");
        msg.setText("Click the link to reset your password:\n"
                + baseUrl + "/auth/users/reset-password?token=" + token);

        mailSender.send(msg);
    }
}