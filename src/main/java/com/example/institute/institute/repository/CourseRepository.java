package com.example.institute.institute.repository;

import com.example.institute.institute.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository  extends JpaRepository<Course, Long> {
    Course findByName(String categoryName);
    Course findByNameAndDescription(String name, String desc);
}
