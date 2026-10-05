package com.example.institute.institute.controller;

import com.example.institute.institute.model.Course;
import com.example.institute.institute.model.request.CourseRequest;
import com.example.institute.institute.model.response.CourseResponse;
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

    private Course toEntity(CourseRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setDuration(request.getDuration());
        return course;
    }

    // CRUD
    //Create - HTTP POST - To create a record (course by admin)
//    @PostMapping("/courses")
//    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
//        System.out.println("Calling createCourse ==> ");
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(courseService.createCourse(course));
//    }

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseRequest request) {
        System.out.println("Calling createCourse ==> ");
        Course saved = courseService.createCourse(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseResponse.from(saved));
    }

    // Read all
//    @GetMapping("/courses")
//    public ResponseEntity<List<Course>> getCourses() {
//        System.out.println("Calling getCourses ==> ");
//        return ResponseEntity.ok(courseService.getCourses());
//    }
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getCourses() {
        System.out.println("Calling getCourses ==> ");
        List<CourseResponse> result = courseService.getCourses().stream()
                .map(CourseResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    // Read one
//    @GetMapping("/courses/{courseId}")
//    public ResponseEntity<Course> getCourse(@PathVariable Long courseId) {
//        System.out.println("Calling getCourse ==> ");
//        return ResponseEntity.ok(courseService.getCourse(courseId));             // 200
//    }
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long courseId) {
        System.out.println("Calling getCourse ==> ");
        return ResponseEntity.ok(CourseResponse.from(courseService.getCourse(courseId)));
    }

    // Update
//    @PutMapping("/courses/{courseId}")
//    public ResponseEntity<Course> updateCourse(@PathVariable Long courseId,
//                                               @RequestBody Course course) {
//        System.out.println("Calling updateCourse ==> ");
//        return ResponseEntity.ok(courseService.updateCourse(courseId, course)); // 200
//    }
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long courseId,
                                                          @RequestBody CourseRequest request) {
        Course updated = courseService.updateCourse(courseId, toEntity(request));
        return ResponseEntity.ok(CourseResponse.from(updated));
    }

    // Delete
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        System.out.println("Calling deleteCourse ==> ");
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();                               // 204
    }
}
