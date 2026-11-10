package com.example.institute.institute.controller;

import com.example.institute.institute.model.Payment;
import com.example.institute.institute.model.request.PaymentRequest;
import com.example.institute.institute.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/reservations/{reservationId}/payment")
    public ResponseEntity<Payment> pay(@PathVariable Long reservationId,
                                       @RequestBody(required = false) PaymentRequest request) {
        String method = (request != null && request.method() != null) ? request.method() : "CARD";
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.pay(reservationId, method));
    }
}