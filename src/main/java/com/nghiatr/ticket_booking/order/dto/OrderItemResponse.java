package com.nghiatr.ticket_booking.order.dto;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderItemResponse(
        UUID orderId,
        UUID customerId,
        double total_amount,
        OrderStatus status,
        LocalDateTime expiredAt,
        int numTicket,
        String customerEmail,
        String customerName,
        String customerPhone
) {
    public static OrderItemResponse from(Order order) {
        return new OrderItemResponse(
                order.getOrderId(),
                order.getCustomer().getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getExpiredAt(),
                order.getNumTicket(),
                order.getCustomerEmail(),
                order.getCustomerName(),
                order.getCustomerPhone()
        );
    }
}
