package com.example.institute.institute.scheduler;

import com.example.institute.institute.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    // تشتغل كل 30 ثانية في thread مستقل (fixedDelay: ما تبدأ مرة ثانية قبل ما تخلص الأولى)
    @Scheduled(fixedDelay = 30000)
    public void releaseExpiredReservations() {
        System.out.println("[" + Thread.currentThread().getName() + "] checking expired reservations...");
        reservationService.expireOverdueReservations();
    }
}