package com.nghiatr.ticket_booking.order.dto;

import com.nghiatr.ticket_booking.order.entity.Order;

import java.util.List;

public record OrderResponse(
        List<OrderItemResponse> orders
) {
    /**
     * Chuyển đổi danh sách thực thể Order sang đối tượng OrderResponse.
     *
     * @param orderList danh sách các thực thể đơn hàng
     * @return đối tượng OrderResponse chứa danh sách chi tiết các đơn hàng
     */
    public static OrderResponse from(List<Order> orderList) {
        return new OrderResponse(
                orderList.stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }
}
