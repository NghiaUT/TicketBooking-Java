package com.nghiatr.ticket_booking.seat.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatLayout {
    Canvas canvas;
    private List<SeatBlock> seatLayout;
}