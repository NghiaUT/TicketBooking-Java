package com.nghiatr.ticket_booking.payment.dto;

import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentCommand(
        UUID paymentId,
        UUID orderId,
        Double amount,
        PaymentMethod method,
        String transactionCode,
        String description,
        String customerEmail,
        String customerName,
        String ipAddress,
        String returnUrl,
        String notifyUrl
) {}
