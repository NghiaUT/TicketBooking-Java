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
    /**
     * Tạo đối tượng CreateOrderResponse từ thực thể Order, danh sách ghế và thời điểm hết hạn giữ chỗ.
     *
     * @param order thực thể đơn hàng đã tạo
     * @param seats danh sách thực thể ghế tương ứng với đơn hàng
     * @param holdExpiredAt thời điểm hết hạn giữ chỗ ghế
     * @return đối tượng CreateOrderResponse hoàn chỉnh
     */
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
