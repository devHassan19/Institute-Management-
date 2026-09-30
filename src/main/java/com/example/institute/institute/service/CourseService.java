package com.example.institute.institute.service;

import com.example.institute.institute.model.Course;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.CourseRepository;
import com.example.institute.institute.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {

    private CourseRepository courseRepository;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }

    public Course createCourse(Course courseObject) {

        System.out.println("Service Calling createCourse ==> ");

        User currentUser = getCurrentLogginUser();

        String role = currentUser.getUserProfile().getRole();

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only admin can create a course");
        }

        return courseRepository.save(courseObject);
    }
}
