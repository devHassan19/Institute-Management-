package com.example.institute.institute.service;

import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.exception.UnprocessableEntityException;
import com.example.institute.institute.model.*;
import com.example.institute.institute.repository.EnrollmrntRepository;
import com.example.institute.institute.repository.PaymentRepository;
import com.example.institute.institute.repository.SeatReservationRepository;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SeatReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final EnrollmrntRepository enrollmrntRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public Payment pay(Long reservationId, String method) {
        User currentUser = EnrollmentService.getCurrentLogginUser();
        Student student = studentRepository.findByUser_Id(currentUser.getId());
        if (student == null) {
            throw new InformationNotFoundException("Student not found");
        }

        SeatReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new InformationNotFoundException("Reservation not found"));

        if (!reservation.getStudent().getId().equals(student.getId())) {
            throw new ForbiddenException("This reservation is not yours");
        }
        if (reservation.getStatus() != ReservationStatus.HELD) {
            throw new InformationExistException("Reservation is already " + reservation.getStatus());
        }
        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnprocessableEntityException("Reservation expired. Please reserve again.");
        }

        Payment payment = new Payment();
        payment.setReservation(reservation);
        BigDecimal amount = reservation.getAClass().getFee();
        if (amount == null) {
            throw new UnprocessableEntityException("This class has no fee set");
        }
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setAClass(reservation.getAClass());
        enrollment = enrollmrntRepository.save(enrollment);

        reservation.setEnrollment(enrollment);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        String className = reservation.getAClass().getName();
        notificationService.sendNotification(currentUser.getId(),
                "Payment received. You enrolled in Class: " + className);
        List<User> admins = userRepository.findByRole("ADMIN");
        for (User admin : admins) {
            notificationService.sendNotification(admin.getId(),
                    "Student " + student.getName() + " paid and enrolled in Class: " + className);
        }
        auditLogService.createLog(currentUser.getId(), "PAYMENT", "Payment", payment.getId(),
                "Student: " + student.getName() + " paid for Class: " + className);

        return payment;
    }
}