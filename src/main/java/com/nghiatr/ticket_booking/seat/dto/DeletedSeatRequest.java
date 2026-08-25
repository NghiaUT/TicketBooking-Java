package com.nghiatr.ticket_booking.seat.dto;

import com.nghiatr.ticket_booking.seat.entity.DeletedSeat;

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
