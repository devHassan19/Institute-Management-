
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

    // Reserves seats in multiple classes for the current student
    public List<BatchReservationResult> reserveMany(List<Long> classIds) {

        // Gets the currently logged-in user
        User user = EnrollmentService.getCurrentLogginUser();

        // Checks if the user's account is active
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Your account has been deactivated");
        }

        // Finds the student linked to the current user
        Student student = studentRepository.findByUser_Id(user.getId());

        // Stops if the student does not exist
        if (student == null) {
            throw new InformationNotFoundException("Student not found");
        }

        // Checks that the student selects between 1 and 5 classes
        if (classIds == null || classIds.isEmpty() || classIds.size() > 5) {
            throw new BadRequestException("Choose between 1 and 5 classes");
        }

        // Removes duplicate class IDs
        List<Long> ids = classIds.stream().distinct().toList();

        // Stores each class reservation result safely between threads
        ConcurrentHashMap<Long, BatchReservationResult> results =
                new ConcurrentHashMap<>();

        // Stores all reservation threads
        List<Thread> threads = new ArrayList<>();

        // Creates a separate task for each class
        for (Long classId : ids) {

            Runnable task = () -> {

                // Prints the thread name and the class being reserved
                System.out.println(Thread.currentThread().getName()
                        + " reserving class " + classId);

                try {
                    // Reserves a seat for the student in this class
                    ReservationResponse r =
                            reservationService.reserveSeat(student.getId(), classId);

                    // Saves the successful reservation result
                    results.put(classId,
                            new BatchReservationResult(
                                    classId, r.status(), r.message()));

                } catch (Exception e) {

                    // Saves the error if the reservation fails
                    results.put(classId,
                            new BatchReservationResult(
                                    classId, "ERROR", e.getMessage()));
                }
            };

            // Creates a new thread for this class
            Thread thread = new Thread(task, "reserve-class-" + classId);

            // Adds the thread to the list
            threads.add(thread);

            // Starts the reservation task
            thread.start();
        }

        // Waits for all reservation threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {

                // Restores the thread's interrupted status
                Thread.currentThread().interrupt();

                // Stops if waiting for the reservations is interrupted
                throw new RuntimeException("Reservation was interrupted");
            }
        }

        // Returns the results in the same order as the class IDs
        return ids.stream().map(results::get).toList();
    }
}
