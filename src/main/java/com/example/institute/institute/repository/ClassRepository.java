package com.example.institute.institute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.institute.institute.model.Class;

import java.util.List;

public interface ClassRepository extends JpaRepository<Class,Long> {
    Class findById(long id);
    Class findByName(String name);
    List<Class> findByNameContainingIgnoreCase(String name);
    List<Class> findByCourseId(Long courseId);

}
