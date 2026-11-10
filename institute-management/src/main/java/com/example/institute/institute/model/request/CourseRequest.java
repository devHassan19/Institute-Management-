package com.example.institute.institute.model.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {
    private String name;
    private String description;
    private Integer duration;
}