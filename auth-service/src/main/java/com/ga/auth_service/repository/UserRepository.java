package com.ga.auth_service.repository;

import com.ga.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    //    For Registration
    Boolean existsByEmailAddress(String emailAddress);

    //    For Login
    User findByEmailAddress(String emailAddress);

    // For Notification
    List<User> findByRole(String role);
}
