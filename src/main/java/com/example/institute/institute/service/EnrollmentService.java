package com.example.institute.institute.service;

import com.example.institute.institute.exception.BadRequestException;
import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.model.Enrollment;
import com.example.institute.institute.model.Instructor;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.EnrollmrntRepository;
import com.example.institute.institute.security.MyUserDetails;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EnrollmentService {
    private EnrollmrntRepository enrollmrntRepository;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }
//    public Enrollment getEnrollment(Long instructorId) {}
public Enrollment createIEnrollment(Enrollment enrollment) {
    User currentUser = getCurrentLogginUser();
    String role = currentUser.getRole();
    System.out.println("Service Calling createEnrollment ==> ");


    Long studentId = enrollment.getStudent().getId();
    Long classId = enrollment.getAClass().getId();

    if (enrollmrntRepository.findByStudentIdAndAClassId(studentId, classId) != null) {
        throw new InformationExistException("Student is already enrolled in this class");
    }

    long enrollmentCount = enrollmrntRepository.countByAClassId(classId);

    int capacity = enrollment.getAClass().getCapacity();

    if (enrollmentCount >= capacity) {
        throw new InformationExistException("Class is full");
    }

    return enrollmrntRepository.save(enrollment);
}

//    public List<Instructor> getInstructors() {
//        System.out.println("Service Calling getInstructor ==> ");
//        return instructorRepository.findAll();
//    }


}
