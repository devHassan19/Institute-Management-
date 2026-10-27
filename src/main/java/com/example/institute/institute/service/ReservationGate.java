
package com.example.institute.institute.service;

import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.model.User;
import com.example.institute.institute.model.UserStatus;
import com.example.institute.institute.model.response.ReservationResponse;
import com.example.institute.institute.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
public class ReservationGate {

    private final ReservationService reservationService;
    private final StudentRepository studentRepository;

    // Allows up to 10 reservation requests at the same time
    private final Semaphore permits = new Semaphore(10);

    // Creates a separate lock for each class
    private final ConcurrentHashMap<Long, ReentrantLock> classLocks =
            new ConcurrentHashMap<>();

    // Checks the current user and reserves a seat for the student
    public ReservationResponse reserve(Long classId) {

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

        // Calls the function that handles the seat reservation
        return reserveSeat(student.getId(), classId);
    }

    // Controls access to the reservation system and the selected class
    public ReservationResponse reserveSeat(Long studentId, Long classId) {

        boolean gotPermit = false;

        // Gets or creates a separate lock for this class
        ReentrantLock lock = classLocks.computeIfAbsent(
                classId, id -> new ReentrantLock(true));

        boolean gotLock = false;

        try {
            // Waits up to 5 seconds to enter the reservation system
            gotPermit = permits.tryAcquire(5, TimeUnit.SECONDS);

            // Stops if the system is too busy
            if (!gotPermit) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "System is busy, try again");
            }

            // Waits up to 5 seconds to lock the selected class
            gotLock = lock.tryLock(5, TimeUnit.SECONDS);

            // Stops if the selected class is busy
            if (!gotLock) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Class is busy, try again");
            }

            // Prints the thread name and the class it locked
            System.out.println(Thread.currentThread().getName()
                    + " holds lock of class " + classId);

            // Calls the service to complete the reservation
            return reservationService.reserveSeat(studentId, classId);

        } catch (InterruptedException e) {

            // Restores the thread's interrupted status
            Thread.currentThread().interrupt();

            // Returns an error if the waiting process is interrupted
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Interrupted");

        } finally {

            // Unlocks the class if the lock was acquired
            if (gotLock) {
                lock.unlock();
            }

            // Releases the permit if it was acquired
            if (gotPermit) {
                permits.release();
            }
        }
    }
}
