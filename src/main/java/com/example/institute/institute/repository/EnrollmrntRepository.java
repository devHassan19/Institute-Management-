package com.example.institute.institute.repository;

import com.example.institute.institute.model.Class;
import com.example.institute.institute.model.Enrollment;
import com.example.institute.institute.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmrntRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByaClass(Class aClass);
}