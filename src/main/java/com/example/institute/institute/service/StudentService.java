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
        Student currentStudent = studentRepository.findById(student.getId())
                .orElseThrow(() ->
                        new InformationNotFoundException(
                                "Student with id " + student.getId() + " not found"));

//        if (!"ADMIN".equals(role)) {
//            throw new ForbiddenException("You can only update your own profile");
//        }

        if (!currentStudent.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own profile");
        }

        currentStudent.setName(student.getName());

        if (student.getUser() != null &&
                student.getUser().getUserProfile() != null) {

            UserProfile newProfile = student.getUser().getUserProfile();
            UserProfile profile = currentUser.getUserProfile();

            profile.setFirstName(newProfile.getFirstName());
            profile.setLastName(newProfile.getLastName());
            profile.setMobileNumber(newProfile.getMobileNumber());
        }

        return studentRepository.save(currentStudent);
    }

}
