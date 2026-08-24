package com.nghiatr.ticket_booking.event.repository;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.user.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findAllByStatus(EventStatus status);
    List<Event> findAllByOrganizer_UserId(UUID organizerId);
    Optional<Event> findByEventIdAndOrganizer_UserId(
            UUID eventId,
            UUID organizerId
    );
}
