package com.example.institute.institute.controller;

import com.example.institute.institute.model.Course;
import com.example.institute.institute.service.CourseService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class CourseController {

    private CourseService courseService;

    // CRUD
    // C - Create - HTTP POST - To create a record (course by admin)

    //    @PostMapping("/courses")
//    public Course createCourse(@RequestBody Course courseObject) {
//        System.out.println("Calling createCourse ==> ");
//        return courseService.createCourse(courseObject);
//    }
    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
                System.out.println("Calling createCourse ==> ");
        return ResponseEntity.status(HttpStatus.CREATED)         // 201
                .body(courseService.createCourse(course));
    }

    // R - Read all
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses() {
        System.out.println("Calling getCourses ==> ");
        return ResponseEntity.ok(courseService.getCourses());                    // 200
    }

    // R - Read one
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<Course> getCourse(@PathVariable Long courseId) {
        System.out.println("Calling getCourse ==> ");
        return ResponseEntity.ok(courseService.getCourse(courseId));             // 200
    }

    // U - Update
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long courseId,
                                               @RequestBody Course course) {
        System.out.println("Calling updateCourse ==> ");
        return ResponseEntity.ok(courseService.updateCourse(courseId, course)); // 200
    }

    // D - Delete
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        System.out.println("Calling deleteCourse ==> ");
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();                               // 204
    }
}
