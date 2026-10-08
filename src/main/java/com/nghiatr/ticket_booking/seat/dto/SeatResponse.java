package com.nghiatr.ticket_booking.seat.dto;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.seat.entity.Seat;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.ticketClass.dto.TicketClassResponse;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;

import java.time.LocalDateTime;
import java.util.UUID;

public record SeatResponse(
        UUID seatId,
        String name,
        SeatStatus status,
        LocalDateTime holdExpiredAt,
        UUID eventId,
        UUID orderId,
        UUID TicketClassId,
        TicketClassResponse ticketClass
) {
    /**
     * Tạo đối tượng SeatResponse từ thực thể Seat và thực thể Order liên quan.
     *
     * @param seat thực thể ghế
     * @param order thực thể đơn hàng sở hữu hoặc giữ ghế
     * @return đối tượng SeatResponse hoàn chỉnh
     */
    public static SeatResponse from(Seat seat, Order order) {
        return new SeatResponse(
                seat.getSeatId(),
                seat.getName(),
                seat.getStatus(),
                seat.getHoldExpiredAt(),
                seat.getEventId().getEventId(),
                order.getOrderId(),
                seat.getTicketClassId().getTicketClassId(),
                TicketClassResponse.toResponse(seat.getTicketClassId())
        );
    }
}
