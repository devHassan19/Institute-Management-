package com.example.institute.institute;

import com.example.institute.institute.model.*;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.model.response.ReservationResponse;
import com.example.institute.institute.repository.*;
import com.example.institute.institute.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ReservationConcurrencyTest {

    private static final int STUDENTS = 50;
    private static final int CAPACITY = 5;

    @Autowired private ReservationService reservationService;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private InstructorRepository instructorRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private SeatReservationRepository reservationRepository;
    @Autowired private WaitlistEntryRepository waitlistRepository;

    private Course course;
    private Instructor instructor;
    private Class aClass;
    private final List<User> users = new ArrayList<>();
    private final List<Long> studentIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setName("Race Course");
        course.setDescription("test");
        course.setDuration(10);
        course = courseRepository.save(course);

        instructor = new Instructor();
        instructor.setName("Race Instructor");
        instructor.setEmail("race@test.com");
        instructor.setPhone("000");
        instructor = instructorRepository.save(instructor);

        aClass = new Class();
        aClass.setName("Race Class");
        aClass.setCapacity(CAPACITY);
        aClass.setCourse(course);
        aClass.setInstructor(instructor);
        aClass = classRepository.save(aClass);

        for (int i = 0; i < STUDENTS; i++) {
            User user = new User();
            user.setUsername("race_user_" + i);
            user.setEmailAddress("race_user_" + i + "@test.com");
            user.setPassword("not-used");
            user.setRole("STUDENT");
            user.setEnabled(true);
            user.setUserStatus(UserStatus.ACTIVE);

            Student student = new Student();
            student.setName("race_user_" + i);
            student.setUser(user);
            user.setStudent(student);

            user = userRepository.save(user);
            users.add(user);
            studentIds.add(user.getStudent().getId());
        }
    }

    @Test
    void fiftyStudentsRaceForFiveSeats() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(STUDENTS);
        CountDownLatch ready = new CountDownLatch(STUDENTS);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger held = new AtomicInteger();
        AtomicInteger waitlisted = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        List<Future<String>> futures = new ArrayList<>();

        for (Long studentId : studentIds) {
            Callable<String> task = () -> {
                ready.countDown();
                go.await(); // الكل ينتظر الإشارة عشان يبدون بنفس اللحظة
                try {
                    ReservationResponse r = reservationService.reserveSeat(studentId, aClass.getId());
                    if ("HELD".equals(r.status())) {
                        held.incrementAndGet();
                    } else {
                        waitlisted.incrementAndGet();
                    }
                    return r.status();
                } catch (Exception e) {
                    errors.incrementAndGet();
                    return "ERROR: " + e.getMessage();
                }
            };
            futures.add(pool.submit(task));
        }

        ready.await();
        go.countDown();
        for (Future<String> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        long inDb = reservationRepository.countActiveHolds(aClass.getId(), ReservationStatus.HELD, LocalDateTime.now());
        System.out.println("HELD = " + held.get() + ", WAITLISTED = " + waitlisted.get()
                + ", ERRORS = " + errors.get() + ", HELD in DB = " + inDb);

        assertEquals(CAPACITY, inDb);
    }

    @AfterEach
    void cleanUp() {
        for (Long id : studentIds) {
            reservationRepository.deleteAll(reservationRepository.findByStudentIdOrderByReservedAtDesc(id));
            waitlistRepository.deleteAll(waitlistRepository.findByStudentId(id));
        }
        userRepository.deleteAll(users);
        classRepository.delete(aClass);
        courseRepository.delete(course);
        instructorRepository.delete(instructor);
    }
}