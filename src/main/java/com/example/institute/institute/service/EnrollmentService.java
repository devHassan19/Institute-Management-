package com.example.institute.institute.service;

import com.example.institute.institute.exception.BadRequestException;
import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.Enrollment;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.model.User;
import com.example.institute.institute.repository.ClassRepository;
import com.example.institute.institute.repository.EnrollmrntRepository;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.security.MyUserDetails;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EnrollmentService {
    private EnrollmrntRepository enrollmrntRepository;
    @Autowired
    private StudentRepository studentRepository;
    private ClassRepository classRepository;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }
//    public Enrollment getEnrollment(Long instructorId) {}
//public Enrollment createIEnrollment(Enrollment enrollment) {
//    User currentUser = getCurrentLogginUser();
//    String role = currentUser.getRole();
//    System.out.println("Service Calling createEnrollment ==> ");
//
//    Student student = studentRepository.findByUser_Id(currentUser.getId());
//
//    if (student == null) {
//        throw new InformationNotFoundException("Student not found");
//    }
//
//    Long studentId = student.getId();
//    Long classId = enrollment.getAClass().getId();
//
//    Class classObject = classRepository.findById(classId)
//            .orElseThrow(() ->
//                    new InformationNotFoundException("Class not found"));
//
//    long enrollmentCount =
//            enrollmrntRepository.countByAClassId(classId);
//
//    int capacity =
//            classObject.getCapacity();
//
//    enrollment.setAClass(classObject);
//
//    if (enrollmrntRepository.findByStudentIdAndAClassId(studentId, classId) != null) {
//        throw new InformationExistException("Student is already enrolled in this class");
//    }
//
//    long enrollmentCount = enrollmrntRepository.countByAClassId(classId);
//    int capacity = enrollment.getAClass().getCapacity();
//    if (enrollmentCount >= capacity) {
//        throw new InformationExistException("Class is full");
//    }
//
//    return enrollmrntRepository.save(enrollment);
//}

    public Enrollment createIEnrollment(Enrollment enrollment) {
        User currentUser = getCurrentLogginUser();
        Student student = studentRepository.findByUser_Id(currentUser.getId());
        System.out.println("Service Calling createEnrollment ==> ");

        if (student == null) {
            throw new InformationNotFoundException("Student not found");
        }

        Long classId = enrollment.getAClass().getId();
        Class aclass = classRepository.findById(classId).orElseThrow(() ->
                new InformationNotFoundException("Class not found"));

        for (Enrollment e : enrollmrntRepository.findByStudent(student)) {
            if (e.getAClass().getId().equals(classId)) {
                throw new InformationExistException("Student is already enrolled in this class");
            }
        }

        int enrolled = enrollmrntRepository.findByaClass(aclass).size();
        if (enrolled >= aclass.getCapacity()) {
            throw new InformationExistException("Class is full");
        }

        enrollment.setStudent(student);
        enrollment.setAClass(aclass);
        return enrollmrntRepository.save(enrollment);
    }
}
