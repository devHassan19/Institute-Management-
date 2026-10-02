package com.example.institute.institute.controller;

import com.example.institute.institute.model.Class;
import com.example.institute.institute.service.ClassService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class ClassController {

    private ClassService classService;

    @PostMapping("/classes")
    public ResponseEntity<Class> createClass(@RequestBody Class classObject) {
        System.out.println("Calling createClass ==> ");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classService.createClass(classObject));
    }

    @GetMapping("/classes")
    public ResponseEntity<List<Class>> getAllClasses() {
        System.out.println("Calling getAllClasses ==> ");
        return ResponseEntity.ok(classService.getClasses());
    }

    @GetMapping("/classes/{classId}")
    public ResponseEntity<Class> getClass(@PathVariable("classId") Long classId) {
        System.out.println("Calling getClass ==> ");
        return ResponseEntity.ok(classService.getClass(classId));
    }

}
