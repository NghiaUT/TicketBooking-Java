package com.nghiatr.ticket_booking.event.entity.seat_layout;

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
