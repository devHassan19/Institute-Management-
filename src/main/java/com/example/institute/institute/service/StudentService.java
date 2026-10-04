package com.example.institute.institute.service;

import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.security.MyUserDetails;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StudentService {

    private StudentRepository studentRepository;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }

    public List<Student> getStudents() {
        User currentUser = getCurrentLogginUser();
        String role = currentUser.getRole();
        System.out.println("Service Calling getStudent ==> ");

        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Only admin can add a instructor");
        }

        String passRole = "STUDENT";
        return  studentRepository.findAllByUser_Role(passRole);
    }


}
