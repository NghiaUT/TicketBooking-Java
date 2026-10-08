package com.nghiatr.ticket_booking.payment.dto;

import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull(message = "Mã đơn hàng không được để trống")
        UUID orderId,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        PaymentMethod method,

        String returnUrl
) {}
