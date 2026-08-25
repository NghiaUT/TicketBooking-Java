package com.nghiatr.ticket_booking.seat.dto;

import com.nghiatr.ticket_booking.seat.entity.SeatBlock;

import java.util.List;
import java.util.UUID;

public record SeatBlockRequest(
        String blockId,
        UUID ticketClassId,
        int rows,
        int cols,
        List<DeletedSeatRequest> deletedSeats,
        PositionRequest position
) {
    public SeatBlock toSeatBlock() {
        return SeatBlock.builder()
                .blockId(blockId)
                .ticketClassId(ticketClassId)
                .rows(rows)
                .cols(cols)
                .deletedSeats(
                        deletedSeats.stream()
                                .map(DeletedSeatRequest::toDeletedSeat)
                                .toList()
                )
                .position(position.toPosition())
                .build();
    }
}
