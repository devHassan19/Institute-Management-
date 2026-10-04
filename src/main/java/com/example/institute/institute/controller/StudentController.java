package com.example.institute.institute.controller;

import com.example.institute.institute.model.Instructor;
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

    @GetMapping("/students/{studentId}")
    public ResponseEntity<Student> getStudent(@PathVariable Long studentId) {
        System.out.println("Calling getStudent ==> ");
        return ResponseEntity.ok(studentService.getStudent(studentId));
    }


    @PutMapping("students/{studentId}")
    public ResponseEntity<Student> update(@PathVariable long studentId,
                                          @RequestBody Student studentObject) {
        System.out.println("Calling updateStudent ==> ");
        studentObject.setId(studentId);

        return ResponseEntity.ok(studentService.updateStudent(studentObject));
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        studentService.delete(id);
//        return ResponseEntity.noContent().build();                            // 204
//    }
}