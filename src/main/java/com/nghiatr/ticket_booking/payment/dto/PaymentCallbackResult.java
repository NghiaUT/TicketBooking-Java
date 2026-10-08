package com.nghiatr.ticket_booking.payment.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record PaymentCallbackResult(
        boolean success,
        String transactionCode,
        String gatewayTransactionId,
        Double amount,
        String responseCode,
        String message,
        Map<String, String> rawData
) {}
