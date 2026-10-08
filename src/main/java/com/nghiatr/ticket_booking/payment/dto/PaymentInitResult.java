package com.nghiatr.ticket_booking.payment.dto;

import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record PaymentInitResult(
        UUID paymentId,
        String paymentUrl,
        PaymentMethod method,
        String transactionCode,
        Map<String, Object> metadata
) {}
