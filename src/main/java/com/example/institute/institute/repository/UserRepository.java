package com.example.institute.institute.repository;

import com.example.institute.institute.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    //    For Registration
    Boolean existsByEmailAddress(String emailAddress);

    //    For Login
    User findByEmailAddress(String emailAddress);

}
