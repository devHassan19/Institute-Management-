package com.example.institute.institute.service;

import com.example.institute.institute.exception.BadRequestException;
import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.*;
import com.example.institute.institute.model.response.BatchReservationResult;
import com.example.institute.institute.model.response.ReservationResponse;
import com.example.institute.institute.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BatchReservationService {

    private final ReservationService reservationService;
    private final StudentRepository studentRepository;

    public List<BatchReservationResult> reserveMany(List<Long> classIds) {
        User user = EnrollmentService.getCurrentLogginUser();
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Your account has been deactivated");
        }
        Student student = studentRepository.findByUser_Id(user.getId());
        if (student == null) {
            throw new InformationNotFoundException("Student not found");
        }
        if (classIds == null || classIds.isEmpty() || classIds.size() > 5) {
            throw new BadRequestException("Choose between 1 and 5 classes");
        }
        List<Long> ids = classIds.stream().distinct().toList();

        ConcurrentHashMap<Long, BatchReservationResult> results = new ConcurrentHashMap<>();
        List<Thread> threads = new ArrayList<>();

        for (Long classId : ids) {
            Runnable task = () -> {
                System.out.println(Thread.currentThread().getName() + " reserving class " + classId);
                try {
                    ReservationResponse r = reservationService.reserveSeat(student.getId(), classId);
                    results.put(classId, new BatchReservationResult(classId, r.status(), r.message()));
                } catch (Exception e) {
                    results.put(classId, new BatchReservationResult(classId, "ERROR", e.getMessage()));
                }
            };
            Thread thread = new Thread(task, "reserve-class-" + classId);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Reservation was interrupted");
            }
        }

        return ids.stream().map(results::get).toList();
    }
}