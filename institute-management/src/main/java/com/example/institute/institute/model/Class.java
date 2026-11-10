package com.example.institute.institute.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "classes")
public class Class {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Integer capacity;

    @Column(precision = 10, scale = 2)
    private BigDecimal fee;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> days;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;


}
