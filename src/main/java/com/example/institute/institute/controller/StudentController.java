package com.example.institute.institute.controller;

import com.example.institute.institute.model.Student;
import com.example.institute.institute.service.StudentService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class StudentController {
    private final StudentService studentService;

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAll() {
        System.out.println("Calling getStudents ==> ");
        return ResponseEntity.ok(studentService.getStudents());
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Student> getById(@PathVariable Long id) {
//        return ResponseEntity.ok(studentService.getById(id));                 // 200
//    }

//    @PostMapping
//    public ResponseEntity<Student> create(@RequestBody Student student) {
//        return ResponseEntity.status(HttpStatus.CREATED)                      // 201
//                .body(studentService.create(student));
//    }

//    @PutMapping("/{id}")
//    public ResponseEntity<Student> update(@PathVariable Long id, @RequestBody Student student) {
//        return ResponseEntity.ok(studentService.update(id, student));         // 200
//    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        studentService.delete(id);
//        return ResponseEntity.noContent().build();                            // 204
//    }
}