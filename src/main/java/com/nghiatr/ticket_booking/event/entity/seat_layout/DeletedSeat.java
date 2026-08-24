package com.nghiatr.ticket_booking.event.entity.seat_layout;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeletedSeat {
    private int row;
    private int col;
}