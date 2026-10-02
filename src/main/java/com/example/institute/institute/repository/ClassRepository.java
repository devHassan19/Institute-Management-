package com.example.institute.institute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.institute.institute.model.Class;
public interface ClassRepository extends JpaRepository<Class,Long> {
    Class findById(long id);
    Class findByName(String name);
}
