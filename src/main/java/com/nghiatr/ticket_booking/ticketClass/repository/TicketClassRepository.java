package com.nghiatr.ticket_booking.ticketClass.repository;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketClassRepository extends JpaRepository<TicketClass, UUID> {
    List<TicketClass> findAllByEventId(Event event);
}
