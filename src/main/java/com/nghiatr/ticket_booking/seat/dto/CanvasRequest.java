package com.nghiatr.ticket_booking.seat.dto;

import com.nghiatr.ticket_booking.seat.entity.Canvas;

public record CanvasRequest(
        int x,
        int y,
        int width,
        int height
) {
    public Canvas toCanvas() {
        return Canvas.builder()
                .x(x)
                .y(y)
                .height(height)
                .width(width)
                .build();
    }
}
