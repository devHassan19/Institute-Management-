package com.example.institute.institute.scheduler;

import com.example.institute.institute.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

//Start again after 30 if first is expired
    @Scheduled(fixedDelay = 30000)
    public void releaseExpiredReservations() {
        System.out.println("[" + Thread.currentThread().getName() + "] checking expired reservations...");
        reservationService.expireOverdueReservations();
    }
}