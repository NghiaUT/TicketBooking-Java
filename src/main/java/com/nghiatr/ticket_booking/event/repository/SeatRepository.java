package com.nghiatr.ticket_booking.event.repository;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    void deleteAllByEventId(Event event);
}
