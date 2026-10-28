
package com.example.institute.institute;

//If you want test use this commend in terminal
//set -a
//source .env
//set +a
//
//mvn -Dtest=ReservationConcurrencyTest test > test-error.log 2>&1

//And this to see result
//grep -E "HELD =|WAITLISTED =|ERRORS =|ACTIVE HOLDS IN DB =|WAITLIST ENTRIES IN DB =|Tests run:|BUILD SUCCESS|BUILD FAILURE|cleanUp" test-error.log | tail -n 30

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
import org.springframework.test.context.ActiveProfiles;
import com.example.institute.institute.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ReservationConcurrencyTest {

    private static final int STUDENTS = 50;
    private static final int CAPACITY = 5;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private SeatReservationRepository reservationRepository;

    @Autowired
    private WaitlistEntryRepository waitlistRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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

        try {
            for (Long studentId : studentIds) {

                Callable<String> task = () -> {
                    ready.countDown();
                    go.await();

                    try {
                        ReservationResponse response =
                                reservationService.reserveSeat(
                                        studentId,
                                        aClass.getId()
                                );

                        if ("HELD".equals(response.status())) {
                            held.incrementAndGet();
                        } else if ("WAITLISTED".equals(response.status())) {
                            waitlisted.incrementAndGet();
                        } else {
                            errors.incrementAndGet();
                        }

                        return response.status();

                    } catch (Exception e) {
                        errors.incrementAndGet();
                        return "ERROR: " + e.getMessage();
                    }
                };

                futures.add(pool.submit(task));
            }

            // Wait until all threads are ready
            ready.await();

            // Start all reservations together
            go.countDown();

            for (Future<String> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }

        } finally {
            pool.shutdownNow();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        }

        long activeHolds = reservationRepository.countActiveHolds(
                aClass.getId(),
                ReservationStatus.HELD,
                LocalDateTime.now()
        );

        long waitingEntries = waitlistRepository
                .findQueue(aClass.getId(), WaitlistStatus.WAITING)
                .size();

        System.out.println("HELD = " + held.get());
        System.out.println("WAITLISTED = " + waitlisted.get());
        System.out.println("ERRORS = " + errors.get());
        System.out.println("ACTIVE HOLDS IN DB = " + activeHolds);
        System.out.println("WAITLIST ENTRIES IN DB = " + waitingEntries);

        assertEquals(CAPACITY, held.get(),
                "Exactly 5 students should reserve seats");

        assertEquals(STUDENTS - CAPACITY, waitlisted.get(),
                "The remaining students should be waitlisted");

        assertEquals(0, errors.get(),
                "There should be no reservation errors");

        assertEquals(CAPACITY, activeHolds,
                "Database should contain exactly 5 active holds");

        assertEquals(STUDENTS - CAPACITY, waitingEntries,
                "Database should contain 45 waiting entries");
    }

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();

        for (Long id : studentIds) {
            reservationRepository.deleteAll(
                    reservationRepository
                            .findByStudentIdOrderByReservedAtDesc(id)
            );

            waitlistRepository.deleteAll(
                    waitlistRepository.findByStudentId(id)
            );
        }

        userRepository.deleteAll(users);

        if (aClass != null) {
            classRepository.delete(aClass);
        }

        if (course != null) {
            courseRepository.delete(course);
        }

        if (instructor != null) {
            instructorRepository.delete(instructor);
        }

        studentIds.clear();
        users.clear();
    }
}
