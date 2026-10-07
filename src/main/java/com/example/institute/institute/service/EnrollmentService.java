package com.example.institute.institute.service;

import com.example.institute.institute.exception.BadRequestException;
import com.example.institute.institute.exception.ForbiddenException;
import com.example.institute.institute.model.*;
import com.example.institute.institute.exception.InformationExistException;
import com.example.institute.institute.exception.InformationNotFoundException;
import com.example.institute.institute.model.Class;
import com.example.institute.institute.repository.ClassRepository;
import com.example.institute.institute.repository.EnrollmrntRepository;
import com.example.institute.institute.repository.StudentRepository;
import com.example.institute.institute.repository.UserRepository;
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
    private AuditLogService auditLogService;
    private UserRepository userRepository;
    private NotificationService notificationService;

    public static User getCurrentLogginUser() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return myUserDetails.getUser();
    }

    public Enrollment createIEnrollment(Enrollment enrollment) {
        User currentUser = getCurrentLogginUser();
        Long userId = currentUser.getId();
        if (currentUser.getUserStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Your account has been deactivated");
        }
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

        Enrollment savedEnrollment = enrollmrntRepository.save(enrollment);
        // Notify Student
        notificationService.sendNotification(currentUser.getId(),
                "You enrolled in Class: " + aclass.getName()
        );

        // Notify Admin
        List<User> admins = userRepository.findByRole("ADMIN");

        for (User admin : admins) {notificationService.sendNotification(admin.getId(),
                    "Student " + student.getName()
                            + " enrolled in Class: " + aclass.getName()
            );
        }

        auditLogService.createLog(userId, "REGISTRATION", "Enrollment",
                savedEnrollment.getId(), "Join Student: " + student.getName() + " With Class: " + aclass.getName());
        return savedEnrollment;
    }

    public List<Enrollment> getEnrollments() {
        System.out.println("Service Calling getInstructor ==> ");
        User currentUser = getCurrentLogginUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can Display all Enrollments");
        }
        return enrollmrntRepository.findAll();
    }

    public Enrollment getEnrollment(Long enrollmentId) {
        User currentUser = getCurrentLogginUser();
        System.out.println("Service Calling getEnrollment ==> ");
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can See This Enrollments");
        }

        return enrollmrntRepository.findById(enrollmentId)
                .orElseThrow(() -> new InformationNotFoundException(
                        "Enrollment with id " + enrollmentId + " not found"));
    }

    public List<Enrollment> getMyEnrollments() {
        User currentUser = getCurrentLogginUser();
        if (currentUser.getUserStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Your account has been deactivated");
        }
        System.out.println("Service Calling getMyEnrollments ==> ");

        Student student = studentRepository.findByUser_Id(currentUser.getId());
        if (student == null) {
            throw new InformationNotFoundException("Student not found");
        }
        return enrollmrntRepository.findByStudent(student);
    }


    public void deleteEnrollment(Long enrollmentId) {
        User currentUser = getCurrentLogginUser();
        Long userId = currentUser.getId();
        System.out.println("Service Calling deleteEnrollment ==> ");
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("Only admin can Delete a Enrollment");
        }

        Enrollment enrollment = getEnrollment(enrollmentId);
        auditLogService.createLog(userId, "DELETE", "Enrollment",
                enrollment.getId(), "DELETE Enrollment To Student: " + enrollment.getStudent().getName() + ", From Class: " + enrollment.getAClass().getName());
        enrollmrntRepository.delete(enrollment);
    }

    public void deleteMyEnrollment(Long enrollmentId) {
        User currentUser = getCurrentLogginUser();
        Long userId = currentUser.getId();
        if (currentUser.getUserStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Your account has been deactivated");
        }
        System.out.println("Service Calling deleteMyEnrollment ==> ");
        Student student = studentRepository.findByUser_Id(currentUser.getId());
        if (student == null) {
            throw new InformationNotFoundException("Student not found");
        }
        Enrollment enrollment = enrollmrntRepository.findById(enrollmentId).orElseThrow(() ->
                new InformationNotFoundException(
                        "Enrollment with id " + enrollmentId + " not found"));


        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new ForbiddenException("You can only delete your own enrollment");
        }

        notificationService.sendNotification(currentUser.getId(),
                "You Drop of Class: " + enrollmentId.getClass().getName()
        );

        // Notify Admin
        List<User> admins = userRepository.findByRole("ADMIN");

        for (User admin : admins) {notificationService.sendNotification(admin.getId(),
                "Student " + student.getName()
                        + " Drop of Class: " + enrollmentId.getClass().getName()
        );
        }
        auditLogService.createLog(userId, "DELETE", "Enrollment",
                enrollment.getId(), "DELETE Enrollment To Student: " + enrollment.getStudent().getName() + ", From Class: " + enrollment.getAClass().getName());
        enrollmrntRepository.delete(enrollment);
    }
}
