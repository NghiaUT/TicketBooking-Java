package com.nghiatr.ticket_booking.seat.entity;

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