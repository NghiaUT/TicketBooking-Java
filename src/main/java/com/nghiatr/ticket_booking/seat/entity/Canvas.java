package com.nghiatr.ticket_booking.seat.entity;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Canvas {
    private int x;
    private int y;
    private int width;
    private int height;
}
