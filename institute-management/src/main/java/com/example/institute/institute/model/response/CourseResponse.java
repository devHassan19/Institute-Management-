package com.example.institute.institute.model.response;

import com.example.institute.institute.model.Course;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private Integer duration;

    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getDuration()
        );
    }
}