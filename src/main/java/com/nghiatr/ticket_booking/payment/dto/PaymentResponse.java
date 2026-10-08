package com.nghiatr.ticket_booking.payment.dto;

import com.nghiatr.ticket_booking.payment.entity.Payment;
import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import com.nghiatr.ticket_booking.payment.entity.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        Double amount,
        PaymentMethod method,
        PaymentStatus status,
        String paymentUrl,
        String transactionCode,
        LocalDateTime createdAt
) {
    /**
     * Tạo đối tượng PaymentResponse từ thực thể Payment và đường dẫn thanh toán.
     *
     * @param payment thực thể thông tin thanh toán
     * @param paymentUrl đường dẫn thanh toán do cổng thanh toán sinh ra
     * @return đối tượng PaymentResponse chứa đầy đủ thông tin thanh toán
     */
    public static PaymentResponse from(Payment payment, String paymentUrl) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paymentUrl(paymentUrl)
                .transactionCode(payment.getTransactionCode())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
