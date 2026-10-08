package com.nghiatr.ticket_booking.seat.dto;

import com.nghiatr.ticket_booking.seat.entity.DeletedSeat;

public record DeletedSeatRequest(
        int row,
        int col
) {
    /**
     * Chuyển đổi dữ liệu DeletedSeatRequest sang thực thể DeletedSeat.
     *
     * @return đối tượng DeletedSeat
     */
    public DeletedSeat toDeletedSeat() {
        return DeletedSeat.builder()
                .col(col)
                .row(row)
                .build();
    }
}
