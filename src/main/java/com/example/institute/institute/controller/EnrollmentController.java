package com.example.institute.institute.controller;

import com.example.institute.institute.model.Enrollment;
import com.example.institute.institute.model.Instructor;
import com.example.institute.institute.service.EnrollmentService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class EnrollmentController {

    private EnrollmentService enrollmentService;

    @PostMapping("/enrollmrnt")
    public ResponseEntity<Enrollment> createIEnrollment(@RequestBody Enrollment enrollment) {
        System.out.println("Calling createEnrollment ==> ");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.createIEnrollment(enrollment));
    }
}
