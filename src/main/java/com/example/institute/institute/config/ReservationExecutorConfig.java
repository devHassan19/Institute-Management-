package com.example.institute.institute.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ReservationExecutorConfig {

    // A fixed pool: at most 10 tasks run at the same time.
    // Extra tasks wait in a queue instead of creating new threads.
    @Bean(destroyMethod = "shutdown")
    public ExecutorService reservationExecutor() {
        // Give each thread a readable name (reserve-pool-1, reserve-pool-2, ...)
        AtomicInteger counter = new AtomicInteger(1);
        return Executors.newFixedThreadPool(10, runnable ->
                new Thread(runnable, "reserve-pool-" + counter.getAndIncrement()));
    }
}