package com.example.institute.institute.controller;

import com.example.institute.institute.model.*;
import com.example.institute.institute.repository.SeatReservationRepository;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.repository.WaitlistEntryRepository;
import com.example.institute.institute.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyReservationController {

    private final SeatReservationRepository reservationRepository;
    private final WaitlistEntryRepository waitlistRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/reservations/my")
    public List<Map<String, Object>> myReservations() {
        Student student = currentStudent();
        return reservationRepository.findByStudentIdOrderByReservedAtDesc(student.getId()).stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("status", r.getStatus().name());
            m.put("reservedAt", r.getReservedAt());
            m.put("expiresAt", r.getExpiresAt());
            m.put("classId", r.getAClass().getId());
            m.put("className", r.getAClass().getName());
            m.put("courseName", r.getAClass().getCourse() != null ? r.getAClass().getCourse().getName() : null);
            m.put("fee", r.getAClass().getFee() != null ? r.getAClass().getFee() : BigDecimal.ZERO);
            return m;
        }).toList();
    }

    @GetMapping("/waitlist/my")
    public List<Map<String, Object>> myWaitlist() {
        Student student = currentStudent();
        return waitlistRepository.findByStudentId(student.getId()).stream()
                .filter(w -> w.getStatus() == WaitlistStatus.WAITING || w.getStatus() == WaitlistStatus.OFFERED)
                .map(w -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", w.getId());
                    m.put("status", w.getStatus().name());
                    m.put("className", w.getAClass().getName());
                    m.put("classId", w.getAClass().getId());
                    long ahead = waitlistRepository.findQueue(w.getAClass().getId(), WaitlistStatus.WAITING)
                            .stream().filter(x -> x.getCreatedAt().isBefore(w.getCreatedAt())).count();
                    m.put("position", ahead + 1);
                    return m;
                }).toList();
    }

    private Student currentStudent() {
        User user = EnrollmentService.getCurrentLogginUser();
        return studentRepository.findByUser_Id(user.getId());
    }
}
