package com.nghiatr.ticket_booking.event.dto.layout_request;

import com.nghiatr.ticket_booking.event.entity.seat_layout.Canvas;
import com.nghiatr.ticket_booking.event.entity.seat_layout.Position;

public record PositionRequest(
        int x,
        int y,
        int width,
        int height
) {
    public Position toPosition() {
        return Position.builder()
                .x(x)
                .y(y)
                .height(height)
                .width(width)
                .build();
    }
}
