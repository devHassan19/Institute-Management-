package com.example.institute.institute.model.request;

import java.util.List;

public record BatchReservationRequest(List<Long> classIds) {
}