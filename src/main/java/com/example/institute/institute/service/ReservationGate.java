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

    // الطبقة 1: أقصى عدد حجوزات تشتغل مع بعض (مثل اتصالات الداتابيس)
    private final Semaphore permits = new Semaphore(10);

    // الطبقة 2: قفل لكل شعبة (fair = true: اللي يوصل أول ياخذ دوره أول)
    private final ConcurrentHashMap<Long, ReentrantLock> classLocks = new ConcurrentHashMap<>();

    // يجيب الطالب من التوكن (للـ controller)
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

    // للـ threads (الـ batch) اللي ما عندها توكن
    public ReservationResponse reserveSeat(Long studentId, Long classId) {
        boolean gotPermit = false;
        ReentrantLock lock = classLocks.computeIfAbsent(classId, id -> new ReentrantLock(true));
        boolean gotLock = false;
        try {
            gotPermit = permits.tryAcquire(5, TimeUnit.SECONDS);
            if (!gotPermit) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "System is busy, try again");
            }
            gotLock = lock.tryLock(5, TimeUnit.SECONDS);
            if (!gotLock) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Class is busy, try again");
            }
            System.out.println(Thread.currentThread().getName() + " holds lock of class " + classId);
            return reservationService.reserveSeat(studentId, classId); // فيها @Transactional وقفل الداتابيس
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Interrupted");
        } finally {
            if (gotLock) {
                lock.unlock();
            }
            if (gotPermit) {
                permits.release();
            }
        }
    }
}