package com.example.institute.institute.controller;

import com.example.institute.institute.model.Course;
import com.example.institute.institute.service.CourseService;
import lombok.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class CourseController {

    private CourseService courseService;

    // CRUD
    // C - Create - HTTP POST - To create a record (course by admin)

    @PostMapping("/courses")
    public Course createCourse(@RequestBody Course courseObject) {
        System.out.println("Calling createCourse ==> ");
        return courseService.createCourse(courseObject);
    }
}
