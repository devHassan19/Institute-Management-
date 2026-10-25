package com.example.institute.institute.controller;

import com.example.institute.institute.model.request.BatchReservationRequest;
import com.example.institute.institute.model.response.BatchReservationResult;
import com.example.institute.institute.model.response.ReservationResponse;
import com.example.institute.institute.service.BatchReservationService;
import com.example.institute.institute.service.ReservationGate;
import com.example.institute.institute.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationGate reservationGate;
    private final BatchReservationService batchReservationService;

    @PostMapping("/classes/{classId}/reservations")
    public ResponseEntity<ReservationResponse> reserve(@PathVariable Long classId) {
        ReservationResponse response = reservationGate.reserve(classId);
        HttpStatus status = "HELD".equals(response.status()) ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/reservations/batch")
    public ResponseEntity<List<BatchReservationResult>> reserveMany(@RequestBody BatchReservationRequest request) {
        return ResponseEntity.ok(batchReservationService.reserveMany(request.classIds()));
    }

}