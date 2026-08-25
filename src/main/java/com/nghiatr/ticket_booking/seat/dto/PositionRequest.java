package com.nghiatr.ticket_booking.seat.dto;

import com.nghiatr.ticket_booking.seat.entity.Position;

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
