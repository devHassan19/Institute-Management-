package com.example.institute.institute.service;

import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.model.User;
import com.example.institute.institute.model.UserProfile;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.security.MyUserDetails;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

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
        return studentRepository.findAll();    }


    public Student getStudent(Long studentId) {
        System.out.println("Service Calling getStudent ==> ");
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new InformationNotFoundException(
                        "Student with id " + studentId + " not found"));
    }

    @Transactional
    public Student updateStudent(Student student) {
        System.out.println("Service Calling updateStudent ==> ");

        User currentUser = getCurrentLogginUser();
        String role = currentUser.getRole();
        Student currentStudent = studentRepository.findById(student.getId()).orElseThrow(() ->
                new InformationNotFoundException(
                        "Student with id " + student.getId() + " not found"));

        if (!currentStudent.getUserId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own profile");
        }

        currentStudent.setName(student.getName());
        return studentRepository.save(currentStudent);
    }

    public Student createStudent(Student student) {
        if (studentRepository.findByUserId(student.getUserId()) != null) {
            throw new ForbiddenException("Student already exists");
        }

        return studentRepository.save(student);
    }

}
