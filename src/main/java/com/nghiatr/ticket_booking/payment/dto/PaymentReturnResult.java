package com.nghiatr.ticket_booking.payment.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentReturnResult(
        boolean success,
        UUID orderId,
        String transactionCode,
        String message
) {}
