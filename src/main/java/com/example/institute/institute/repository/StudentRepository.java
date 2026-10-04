package com.example.institute.institute.repository;

import com.example.institute.institute.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findByName(String studentName);
    List<Student> findAllByUser_Role(String role);
//    Student findbyId(Long id);
}
