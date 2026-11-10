
package com.example.institute.institute.config;

import com.example.institute.institute.model.Course;
import com.example.institute.institute.model.Instructor;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.repository.CourseRepository;
import com.example.institute.institute.repository.InstructorRepository;
import com.example.institute.institute.repository.ClassRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final ClassRepository classRepository;

    public DataSeeder(
            CourseRepository courseRepository,
            InstructorRepository instructorRepository,
            ClassRepository classRepository) {

        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
        this.classRepository = classRepository;
    }

    @Override
    public void run(String... args) {

        // Seed Instructors
        Instructor instructor1 = instructorRepository.findByName("Ahmed Ali");

        if (instructor1 == null) {
            instructor1 = new Instructor();
            instructor1.setName("Ahmed Ali");
            instructor1.setEmail("ahmed@example.com");
            instructor1.setPhone("39000001");
            instructor1 = instructorRepository.save(instructor1);
        }

        Instructor instructor2 =  instructorRepository.findByName("Sara Hassan");

        if (instructor2 == null) {
            instructor2 = new Instructor();
            instructor2.setName("Sara Hassan");
            instructor2.setEmail("sara@example.com");
            instructor2.setPhone("39000002");
            instructor2 = instructorRepository.save(instructor2);
        }

        // Seed Courses
        Course javaCourse =  courseRepository.findByName("Java Programming");

        if (javaCourse == null) {
            javaCourse = new Course();
            javaCourse.setName("Java Programming");
            javaCourse.setDescription("Learn Java programming");
            javaCourse.setDuration(8);
            javaCourse = courseRepository.save(javaCourse);
        }

        Course databaseCourse =  courseRepository.findByName("Database Systems");

        if (databaseCourse == null) {
            databaseCourse = new Course();
            databaseCourse.setName("Database Systems");
            databaseCourse.setDescription("Learn database concepts");
            databaseCourse.setDuration(6);
            databaseCourse = courseRepository.save(databaseCourse);
        }

        // Seed Classes
        if (classRepository.findByName("Java Class A") == null) {
            Class javaClass = new Class();

            javaClass.setName("Java Class A");
            javaClass.setCapacity(20);
            javaClass.setFee(new BigDecimal("150.00"));
            javaClass.setStartDate(LocalDate.of(2027, 1, 10));
            javaClass.setEndDate(LocalDate.of(2027, 3, 10));
            javaClass.setDays(Set.of(
                    DayOfWeek.SUNDAY,
                    DayOfWeek.TUESDAY
            ));
            javaClass.setCourse(javaCourse);
            javaClass.setInstructor(instructor1);

            classRepository.save(javaClass);
        }

        if (classRepository.findByName("Database Class A") == null) {
            Class databaseClass = new Class();

            databaseClass.setName("Database Class A");
            databaseClass.setCapacity(15);
            databaseClass.setFee(new BigDecimal("120.00"));
            databaseClass.setStartDate(LocalDate.of(2027, 1, 15));
            databaseClass.setEndDate(LocalDate.of(2027, 3, 15));
            databaseClass.setDays(Set.of(
                    DayOfWeek.MONDAY,
                    DayOfWeek.WEDNESDAY
            ));
            databaseClass.setCourse(databaseCourse);
            databaseClass.setInstructor(instructor2);

            classRepository.save(databaseClass);
        }

        System.out.println("Seed data completed successfully!");
    }
}
