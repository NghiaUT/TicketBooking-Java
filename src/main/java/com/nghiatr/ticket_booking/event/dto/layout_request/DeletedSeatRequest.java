package com.nghiatr.ticket_booking.event.dto.layout_request;

import com.nghiatr.ticket_booking.event.entity.seat_layout.DeletedSeat;

public record DeletedSeatRequest(
        int row,
        int col
) {
    public DeletedSeat toDeletedSeat() {
        return DeletedSeat.builder()
                .col(col)
                .row(row)
                .build();
    }
}
