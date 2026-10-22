package com.example.institute.institute.controller;

import com.example.institute.institute.model.response.ReservationResponse;
import com.example.institute.institute.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/classes/{classId}/reservations")
    public ResponseEntity<ReservationResponse> reserve(@PathVariable Long classId) {
        ReservationResponse response = reservationService.reserve(classId);
        HttpStatus status = "HELD".equals(response.status()) ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}