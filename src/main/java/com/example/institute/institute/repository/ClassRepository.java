package com.example.institute.institute.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.institute.institute.model.Class;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<Class,Long> {
    Class findById(long id);
    Class findByName(String name);
    List<Class> findByNameContainingIgnoreCase(String name);
    List<Class> findByCourseId(Long courseId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Class> findWithLockById(Long id);
}
