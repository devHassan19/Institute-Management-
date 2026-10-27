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

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseRequest request) {
        System.out.println("Calling createCourse ==> ");
        Course saved = courseService.createCourse(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseResponse.from(saved));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getCourses() {
        System.out.println("Calling getCourses ==> ");
        List<CourseResponse> result = courseService.getCourses().stream()
                .map(CourseResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long courseId) {
        System.out.println("Calling getCourse ==> ");
        return ResponseEntity.ok(CourseResponse.from(courseService.getCourse(courseId)));
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long courseId,
                                                          @RequestBody CourseRequest request) {
        Course updated = courseService.updateCourse(courseId, toEntity(request));
        return ResponseEntity.ok(CourseResponse.from(updated));
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        System.out.println("Calling deleteCourse ==> ");
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}
