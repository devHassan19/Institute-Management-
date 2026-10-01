package com.example.institute.institute.controller;

import com.example.institute.institute.model.Course;
import com.example.institute.institute.model.Instructor;
import com.example.institute.institute.service.InstructorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class InstructorController {

    private InstructorService instructorService;

    @PostMapping("/instructors")
    public ResponseEntity<Instructor> createInstructor(@RequestBody Instructor instructor) {
        System.out.println("Calling createInstructor ==> ");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(instructorService.createInstructor(instructor));
    }

    @GetMapping("/instructors")
    public ResponseEntity<List<Instructor>> getAllInstructors() {
        System.out.println("Calling getAllInstructors => ");
        return ResponseEntity.ok(instructorService.getInstructors());
    }

    @GetMapping("/instructors/{instructorId}")
    public ResponseEntity<Instructor> getInstructor(@PathVariable Long instructorId) {
        System.out.println("Calling getInstructor ==> ");
        return ResponseEntity.ok(instructorService.getInstructor(instructorId));
    }

    @PutMapping("/instructors/{instructorId}")
    public ResponseEntity<Instructor> updateInstructor(@PathVariable Long instructorId,
                                                       @RequestBody Instructor instructor) {
        System.out.println("Calling updateInstructor ==> ");
        return ResponseEntity.ok(instructorService.updateInstructor(instructorId, instructor));
    }

    @DeleteMapping("/instructors/{instructorId}")
    public ResponseEntity<Instructor> deleteInstructor(@PathVariable Long instructorId) {
        System.out.println("Calling deleteInstructor ==> ");
        instructorService.deleteInstructor(instructorId);
        return ResponseEntity.noContent().build();
    }

}
