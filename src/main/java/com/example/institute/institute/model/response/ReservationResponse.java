package com.example.institute.institute.model.response;

import java.time.LocalDateTime;

public record ReservationResponse(String status, Long reservationId, LocalDateTime expiresAt,
                                  Integer waitlistPosition, String message) {
}