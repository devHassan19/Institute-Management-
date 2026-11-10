package com.example.institute.institute.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // SEAT_OFFERED, RESERVATION_EXPIRED, PAYMENT_CONFIRMED ...
    private String type;

    private String message;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}