package com.example.institute.institute.service;

import com.example.institute.institute.exception.BadRequestException;
import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.*;
import com.example.institute.institute.model.response.BatchReservationResult;
import com.example.institute.institute.model.response.ReservationResponse;
import com.example.institute.institute.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchReservationService {

    // Maximum number of classes a student can choose at once
    private static final int MAX_CLASSES = 5;

    // Maximum time we wait for one class reservation
    private static final long TASK_TIMEOUT_SECONDS = 30;

    private final ReservationGate reservationGate;
    private final StudentRepository studentRepository;

    // The shared thread pool (created in ReservationExecutorConfig)
    private final ExecutorService reservationExecutor;

    // Reserves seats in several classes for the current student
    public List<BatchReservationResult> reserveMany(List<Long> classIds) {

        // Step 1: runs in the request thread.
        // Get the logged-in user from the security context.
        // This must happen here, because pool threads do not have the security context.
        User user = EnrollmentService.getCurrentLogginUser();

        // Stop if the account is not active
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Your account has been deactivated");
        }

        // Find the student linked to this user
        Student student = studentRepository.findByUser_Id(user.getId());

        // Stop if there is no student profile
        if (student == null) {
            throw new InformationNotFoundException("Student not found");
        }

        // Check that the student chose between 1 and 5 classes
        if (classIds == null || classIds.isEmpty() || classIds.size() > MAX_CLASSES) {
            throw new BadRequestException("Choose between 1 and " + MAX_CLASSES + " classes");
        }

        // Remove duplicate class ids and keep the original order
        List<Long> ids = classIds.stream().distinct().toList();

        // Keep the student id in a local variable so the tasks can use it
        Long studentId = student.getId();

        log.info("Batch reservation started: studentId={}, classes={}", studentId, ids);
        long start = System.currentTimeMillis();

        // Step 2: send one task per class to the thread pool.
        // Each task is a Callable. submit() gives back a Future,
        // which is a "ticket" we use later to get the result.
        // LinkedHashMap keeps the same order as the classes the student chose.
        Map<Long, Future<BatchReservationResult>> futures = new LinkedHashMap<>();
        for (Long classId : ids) {
            futures.put(classId, reservationExecutor.submit(() -> reserveOne(studentId, classId)));
        }

        // Step 3: collect the results.
        // Each Future has its own timeout, so one slow class
        // cannot block the whole request forever.
        List<BatchReservationResult> results = new ArrayList<>();
        for (Map.Entry<Long, Future<BatchReservationResult>> entry : futures.entrySet()) {
            results.add(awaitResult(entry.getKey(), entry.getValue()));
        }

        log.info("Batch reservation finished: studentId={}, took={}ms, results={}",
                studentId,
                System.currentTimeMillis() - start,
                results.stream().map(r -> r.classId() + "=" + r.status()).toList());

        return results;
    }

    // Runs inside one pool thread (for example reserve-pool-3).
    // Every error is caught here, so one failed class does not stop the others.
    private BatchReservationResult reserveOne(Long studentId, Long classId) {
        log.debug("[{}] reserving class {}", Thread.currentThread().getName(), classId);
        try {
            // Reserve a seat (this uses the semaphore, the class lock, and the database lock)
            ReservationResponse response = reservationGate.reserveSeat(studentId, classId);
            return new BatchReservationResult(classId, response.status(), response.message());
        } catch (Exception e) {
            // Save the error as a result, so the student still gets an answer for this class
            log.warn("Reservation failed for class {} (student {}): {}", classId, studentId, e.getMessage());
            return new BatchReservationResult(classId, "ERROR", e.getMessage());
        }
    }

    // Waits for one Future and turns any problem into an ERROR result
    private BatchReservationResult awaitResult(Long classId, Future<BatchReservationResult> future) {
        try {
            // Wait for the result, but no longer than the timeout
            return future.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            // The task took too long: try to cancel it and report a timeout
            future.cancel(true);
            log.warn("Reservation task timed out for class {}", classId);
            return new BatchReservationResult(classId, "ERROR", "Timed out, please try again");

        } catch (InterruptedException e) {
            // The waiting thread was interrupted: keep the interrupt flag and report it
            Thread.currentThread().interrupt();
            return new BatchReservationResult(classId, "ERROR", "Interrupted");

        } catch (ExecutionException e) {
            // The task itself threw an exception: report its message
            return new BatchReservationResult(classId, "ERROR", String.valueOf(e.getCause().getMessage()));
        }
    }
}