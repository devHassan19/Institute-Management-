package com.example.institute.institute;

import com.example.institute.institute.exception.BadRequestException;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.ClassRepository;
import com.example.institute.institute.repository.CourseRepository;
import com.example.institute.institute.repository.InstructorRepository;
import com.example.institute.institute.security.MyUserDetails;
import com.example.institute.institute.service.AuditLogService;
import com.example.institute.institute.service.ClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.Course;
import com.example.institute.institute.model.Instructor;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Optional;

class ClassServiceTest {

    ClassService classService;
    ClassRepository classRepository;
    CourseRepository courseRepository;
    InstructorRepository instructorRepository;

    @BeforeEach
    public void setUp() {
        classRepository = mock(ClassRepository.class);
        courseRepository = mock(CourseRepository.class);
        instructorRepository = mock(InstructorRepository.class);
        AuditLogService auditLogService = mock(AuditLogService.class);

        classService = new ClassService(
                classRepository,
                courseRepository,
                instructorRepository,
                auditLogService
        );

        User user = new User();
        user.setId(1L);
        user.setRole("ADMIN");

        MyUserDetails userDetails = new MyUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null)
        );
    }

    @Test
    @DisplayName("When class name is empty then exception is thrown")
    public void whenClassNameIsEmptyThenExceptionIsThrown() {
        Class classObject = new Class();
        classObject.setName("");

        assertThrows(
                BadRequestException.class,
                () -> classService.createClass(classObject)
        );
    }


    @Test
    @DisplayName("When class fee is negative then exception is thrown")
    public void whenClassFeeIsNegativeThenExceptionIsThrown() {
        Class classObject = new Class();
        classObject.setName("Java Class");
        classObject.setFee(new java.math.BigDecimal("-10"));

        assertThrows(
                BadRequestException.class,
                () -> classService.createClass(classObject)
        );
    }

    @Test
    @DisplayName("When class name already exists then exception is thrown")
    public void whenClassNameAlreadyExistsThenExceptionIsThrown() {

        Class classObject = new Class();
        classObject.setName("Java Class");
        classObject.setFee(new java.math.BigDecimal("100"));

        when(classRepository.findByName("Java Class"))
                .thenReturn(new Class());

        assertThrows(
                InformationExistException.class,
                () -> classService.createClass(classObject)
        );
    }

    @Test
    @DisplayName("When course does not exist then exception is thrown")
    public void whenCourseDoesNotExistThenExceptionIsThrown() {

        Class classObject = new Class();
        classObject.setName("Java Class");
        classObject.setFee(new java.math.BigDecimal("100"));

        Course course = new Course();
        course.setId(1L);
        classObject.setCourse(course);

        Instructor instructor = new Instructor();
        instructor.setId(2L);
        classObject.setInstructor(instructor);

        when(courseRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                InformationNotFoundException.class,
                () -> classService.createClass(classObject)
        );
    }

    @Test
    @DisplayName("When instructor does not exist then exception is thrown")
    public void whenInstructorDoesNotExistThenExceptionIsThrown() {

        Class classObject = new Class();
        classObject.setName("Java Class");
        classObject.setFee(new java.math.BigDecimal("100"));

        Course course = new Course();
        course.setId(1L);
        classObject.setCourse(course);

        Instructor instructor = new Instructor();
        instructor.setId(2L);
        classObject.setInstructor(instructor);

        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));

        when(instructorRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                InformationNotFoundException.class,
                () -> classService.createClass(classObject)
        );
    }
}
