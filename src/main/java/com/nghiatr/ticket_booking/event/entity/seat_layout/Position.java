package com.nghiatr.ticket_booking.event.entity.seat_layout;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Position {

    private int x;
    private int y;
    private int width;
    private int height;
}