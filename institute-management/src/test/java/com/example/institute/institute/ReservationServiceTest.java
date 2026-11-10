package com.example.institute.institute;

import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.model.User;
import com.example.institute.institute.model.UserStatus;
import com.example.institute.institute.repository.ClassRepository;
import com.example.institute.institute.repository.EnrollmrntRepository;
import com.example.institute.institute.repository.SeatReservationRepository;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.repository.WaitlistEntryRepository;
import com.example.institute.institute.service.NotificationService;
import com.example.institute.institute.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationService reservationService;
    private StudentRepository studentRepository;
    private ClassRepository classRepository;
    private SeatReservationRepository reservationRepository;
    private EnrollmrntRepository enrollmrntRepository;
    private WaitlistEntryRepository waitlistRepository;
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        reservationRepository = mock(SeatReservationRepository.class);
        waitlistRepository = mock(WaitlistEntryRepository.class);
        enrollmrntRepository = mock(EnrollmrntRepository.class);
        classRepository = mock(ClassRepository.class);
        studentRepository = mock(StudentRepository.class);
        notificationService = mock(NotificationService.class);

        reservationService = new ReservationService(
                reservationRepository,
                waitlistRepository,
                enrollmrntRepository,
                classRepository,
                studentRepository,
                notificationService
        );
    }

    @Test
    @DisplayName("When student does not exist then exception is thrown")
    public void whenStudentDoesNotExistThenExceptionIsThrown() {
        when(studentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                InformationNotFoundException.class,
                () -> reservationService.reserveSeat(1L, 1L)
        );

        verify(classRepository, never()).findWithLockById(anyLong());
    }

    @Test
    @DisplayName("When class does not exist then exception is thrown")
    public void whenClassDoesNotExistThenExceptionIsThrown() {
        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(new Student()));

        when(classRepository.findWithLockById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                InformationNotFoundException.class,
                () -> reservationService.reserveSeat(1L, 1L)
        );
    }
}
