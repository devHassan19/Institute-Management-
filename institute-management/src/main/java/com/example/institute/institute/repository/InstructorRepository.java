package com.example.institute.institute.repository;

import com.example.institute.institute.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Instructor findByName(String name);
    Instructor findByNameAndEmail(String name, String email);}
