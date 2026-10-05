package com.example.institute.institute.repository;

import com.example.institute.institute.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmrntRepository extends JpaRepository<Enrollment , Long> {
    Enrollment findByStudentIdAndAClassId(Long studentId, Long classId);
    long countByAClassId(Long classId);

}
