package com.nghiatr.ticket_booking.event.dto.layout_request;

import com.nghiatr.ticket_booking.event.entity.seat_layout.Canvas;

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
