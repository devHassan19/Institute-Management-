package com.example.institute.institute.service;

import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.*;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.model.response.ReservationResponse;
import com.example.institute.institute.repository.ClassRepository;
import com.example.institute.institute.repository.EnrollmrntRepository;
import com.example.institute.institute.repository.SeatReservationRepository;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.repository.WaitlistEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final SeatReservationRepository reservationRepository;
    private final WaitlistEntryRepository waitlistRepository;
    private final EnrollmrntRepository enrollmrntRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;


    @Value("${institute.reservation-minutes:10}")
    private long holdMinutes;

    @Transactional
    public ReservationResponse reserve(Long classId) {
        User user = EnrollmentService.getCurrentLogginUser();
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Your account has been deactivated");
        }
        Student student = studentRepository.findByUser_Id(user.getId());
        if (student == null) {
            throw new InformationNotFoundException("Student not found");
        }
        return reserveSeat(student.getId(), classId);
    }

    @Transactional
    public ReservationResponse reserveSeat(Long studentId, Long classId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new InformationNotFoundException("Student not found"));
        Class aClass = classRepository.findWithLockById(classId)
                .orElseThrow(() -> new InformationNotFoundException("Class not found"));

        boolean alreadyEnrolled = enrollmrntRepository.findByStudent(student).stream()
                .anyMatch(e -> e.getAClass().getId().equals(classId));
        if (alreadyEnrolled) {
            throw new InformationExistException("Student is already enrolled in this class");
        }

        LocalDateTime now = LocalDateTime.now();
        if (reservationRepository.hasActiveHold(student.getId(), classId, ReservationStatus.HELD, now)) {
            throw new InformationExistException("You already have an active reservation for this class");
        }

        long taken = enrollmrntRepository.findByaClass(aClass).size()
                + reservationRepository.countActiveHolds(classId, ReservationStatus.HELD, now);

        if (taken < aClass.getCapacity()) {
            SeatReservation reservation = new SeatReservation();
            reservation.setStudent(student);
            reservation.setAClass(aClass);
            reservation.setStatus(ReservationStatus.HELD);
            reservation.setReservedAt(now);
            reservation.setExpiresAt(now.plusMinutes(holdMinutes));
            reservationRepository.save(reservation);
            notificationService.sendNotification(student.getUser().getId(),
                    "Your seat has been reserved successfully."
            );
            return new ReservationResponse("HELD", reservation.getId(), reservation.getExpiresAt(), null,
                    "Seat held for " + holdMinutes + " minutes. Complete payment before it expires.");
        }

        if (waitlistRepository.existsByStudentAndClassAndStatus(student.getId(), classId, WaitlistStatus.WAITING)) {
            throw new InformationExistException("You are already on the waiting list for this class");
        }
        WaitlistEntry entry = new WaitlistEntry();
        entry.setStudent(student);
        entry.setAClass(aClass);
        entry.setStatus(WaitlistStatus.WAITING);
        entry.setCreatedAt(now);
        waitlistRepository.save(entry);

        int position = waitlistRepository.findQueue(classId, WaitlistStatus.WAITING).size();
        notificationService.sendNotification(student.getUser().getId(),
                "The class is full. You have been added to the waiting list. Your position is: " + position
        );
        return new ReservationResponse("WAITLISTED", null, null, position,
                "Class is full. You are number " + position + " on the waiting list.");
    }
}