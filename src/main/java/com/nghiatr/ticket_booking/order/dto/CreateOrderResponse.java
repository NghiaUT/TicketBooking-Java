package com.nghiatr.ticket_booking.order.dto;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.seat.dto.SeatResponse;
import com.nghiatr.ticket_booking.seat.entity.Seat;

import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderResponse(
        OrderItemResponse order,
        //PaymentItemResponse payment,
        List<SeatResponse> seats,
        LocalDateTime holdExpiredAt
) {
    public static CreateOrderResponse from(Order order, List<Seat> seats, LocalDateTime holdExpiredAt) {
        return new CreateOrderResponse(
                OrderItemResponse.from(order),
                seats.stream()
                        .map(seat -> SeatResponse.from(seat, order))
                        .toList(),
                holdExpiredAt
        );
    }
}
