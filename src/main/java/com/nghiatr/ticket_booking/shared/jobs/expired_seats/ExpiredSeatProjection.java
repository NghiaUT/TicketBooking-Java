package com.nghiatr.ticket_booking.shared.jobs.expired_seats;

import com.nghiatr.ticket_booking.event.entity.Event;

import java.util.UUID;

public interface ExpiredSeatProjection {
    UUID getSeatId();
    Event getEventId();
}
