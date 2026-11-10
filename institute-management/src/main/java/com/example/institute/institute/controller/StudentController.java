package com.example.institute.institute.controller;

import com.example.institute.institute.model.Instructor;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.service.StudentService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.institute.institute.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentController {

    private final StudentService studentService;

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAll() {
        System.out.println("Calling getStudents ==> ");
        return ResponseEntity.ok(studentService.getStudents());
    }

    @PostMapping("/students")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return ResponseEntity.ok(studentService.createStudent(student));
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

    @PostMapping("/students/internal")
    public ResponseEntity<Student> createStudentInternal(
            @RequestBody Student student,
            @RequestHeader("X-Internal-Secret") String secret) {

        if (!Objects.equals(internalApiSecret, secret)) {
            throw new UnauthorizedException("Invalid internal secret");
        }

        return ResponseEntity.ok(studentService.createStudent(student));
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        studentService.delete(id);
//        return ResponseEntity.noContent().build();                            // 204
//    }
}