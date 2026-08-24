package com.nghiatr.ticket_booking.shared.dto;

import java.time.Instant;

public record ErrorResponse (
    int status,
    String error,
    String errorCode,
    String message,
    String path,
    Instant timestamp
) {}
