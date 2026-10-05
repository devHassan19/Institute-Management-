package com.example.institute.institute.controller;

import com.example.institute.institute.model.Enrollment;
import com.example.institute.institute.model.Instructor;
import com.example.institute.institute.service.EnrollmentService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class EnrollmentController {

    private EnrollmentService enrollmentService;

    @PostMapping("/enrollmrnts")
    public ResponseEntity<Enrollment> createIEnrollment(@RequestBody Enrollment enrollment) {
        System.out.println("Calling createEnrollment ==> ");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.createIEnrollment(enrollment));
    }

    @GetMapping("enrollmrnts")
    public List<Enrollment> getEnrollments() {
        System.out.println("Calling getEnrollment ==> ");
        return enrollmentService.getEnrollments();
    }

    @GetMapping("/enrollmrnts/{enrollmentId}")
    public Enrollment getEnrollment(@PathVariable Long enrollmentId) {
        System.out.println("Calling getMyEnrollments ==> ");
        return enrollmentService.getEnrollment(enrollmentId);
    }

    @GetMapping("/myEnrollmrnt")
    public List<Enrollment> getMyEnrollments() {
        System.out.println("Calling getMyEnrollments ==> ");
        return enrollmentService.getMyEnrollments();
    }

    @DeleteMapping("/enrollmrnts/{enrollmentId}")
    public ResponseEntity<Enrollment> deleteEnrollment(@PathVariable Long enrollmentId) {
        System.out.println("Calling deleteEnrollment ==> ");
        enrollmentService.deleteEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }
    
}
