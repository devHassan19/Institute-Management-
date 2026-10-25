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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final SeatReservationRepository reservationRepository;
    private final WaitlistEntryRepository waitlistRepository;
    private final EnrollmrntRepository enrollmrntRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;


    @Value("${institute.reservation-minutes:1}")
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

    // تشتغل من المهمة المجدولة: تلغي الحجوزات المنتهية وتعطي المقعد لأول منتظر
    @Transactional
    public void expireOverdueReservations() {
        List<SeatReservation> overdue = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.HELD, LocalDateTime.now());

        for (SeatReservation candidate : overdue) {
            // نقفل الحجز ونتأكد إنه لسه HELD (يمكن الطالب دفع بنفس اللحظة)
            SeatReservation r = reservationRepository.findWithLockById(candidate.getId()).orElse(null);
            if (r == null || r.getStatus() != ReservationStatus.HELD
                    || r.getExpiresAt().isAfter(LocalDateTime.now())) {
                continue;
            }

            r.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(r);
            notificationService.sendNotification(r.getStudent().getUser().getId(),
                    "Your reservation for class " + r.getAClass().getName() + " has expired.");

            // لو الطالب جاي من الانتظار وما دفع، نلغي دوره
            for (WaitlistEntry w : waitlistRepository.findEntries(
                    r.getStudent().getId(), r.getAClass().getId(), WaitlistStatus.OFFERED)) {
                w.setStatus(WaitlistStatus.CANCELLED);
                waitlistRepository.save(w);
            }

            promoteNextInQueue(r.getAClass().getId());
        }
    }

    // يعطي المقعد لأول واحد في الانتظار (الأقدم)
    private void promoteNextInQueue(Long classId) {
        Class aClass = classRepository.findWithLockById(classId).orElse(null);
        if (aClass == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        long taken = enrollmrntRepository.findByaClass(aClass).size()
                + reservationRepository.countActiveHolds(classId, ReservationStatus.HELD, now);
        if (taken >= aClass.getCapacity()) {
            return;
        }

        List<WaitlistEntry> queue = waitlistRepository.findQueue(classId, WaitlistStatus.WAITING);
        if (queue.isEmpty()) {
            return;
        }
        WaitlistEntry next = queue.get(0);

        SeatReservation offer = new SeatReservation();
        offer.setStudent(next.getStudent());
        offer.setAClass(aClass);
        offer.setStatus(ReservationStatus.HELD);
        offer.setReservedAt(now);
        offer.setExpiresAt(now.plusMinutes(holdMinutes));
        reservationRepository.save(offer);

        next.setStatus(WaitlistStatus.OFFERED);
        waitlistRepository.save(next);

        notificationService.sendNotification(next.getStudent().getUser().getId(),
                "A seat is now available in class " + aClass.getName()
                        + ". Pay within " + holdMinutes + " minutes to confirm it.");
    }
}