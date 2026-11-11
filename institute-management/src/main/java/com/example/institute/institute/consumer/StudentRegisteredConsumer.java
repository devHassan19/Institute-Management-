
package com.example.institute.institute.consumer;


import com.example.institute.institute.event.StudentRegisteredEvent;
import com.example.institute.institute.model.Student;
import com.example.institute.institute.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class StudentRegisteredConsumer {

    private final StudentService studentService;

    @KafkaListener(
            topics = "student-registered",
            groupId = "institute-group"
    )
    public void consume(StudentRegisteredEvent event) {

        Student student = new Student();
        student.setUserId(event.userId());
        student.setName(event.username());

        studentService.createStudent(student);

        System.out.println("Student created: " + event.username());
    }
}
