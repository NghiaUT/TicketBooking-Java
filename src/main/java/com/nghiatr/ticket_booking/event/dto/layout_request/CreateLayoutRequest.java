package com.nghiatr.ticket_booking.event.dto.layout_request;

import com.nghiatr.ticket_booking.event.entity.seat_layout.SeatLayout;

import java.util.List;

public record CreateLayoutRequest(
        CanvasRequest canvas,
        List<SeatBlockRequest> seatLayout
) {
    public SeatLayout toSeatLayout() {
        return SeatLayout.builder()
                .seatLayout(
                        seatLayout.stream()
                                .map(SeatBlockRequest::toSeatBlock)
                                .toList()
                )
                .canvas(canvas.toCanvas())
                .build();
    }
}
