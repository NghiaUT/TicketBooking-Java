package com.nghiatr.ticket_booking.order.dto;

import com.nghiatr.ticket_booking.order.entity.Order;

import java.util.List;

public record OrderResponse(
        List<OrderItemResponse> orders
) {
    public static OrderResponse from(List<Order> orderList) {
        return new OrderResponse(
                orderList.stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }
}
