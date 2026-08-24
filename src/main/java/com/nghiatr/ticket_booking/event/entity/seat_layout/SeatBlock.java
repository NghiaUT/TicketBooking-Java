package com.nghiatr.ticket_booking.event.entity.seat_layout;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatBlock {

    private String blockId;
    private UUID ticketClassId;
    private int rows;
    private int cols;
    private List<DeletedSeat> deletedSeats;
    Position position;
}
