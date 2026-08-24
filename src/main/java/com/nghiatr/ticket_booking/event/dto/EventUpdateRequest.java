package com.nghiatr.ticket_booking.event.dto;

import com.nghiatr.ticket_booking.event.entity.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventUpdateRequest(
        String eventName,
        String description,
        EventStatus status,
        LocalDateTime dateToStart,
        LocalDateTime timeToStart,
        UUID venueId,
        List<TicketClassUpdateRequest> ticketClasses
) {
    public boolean onlyContainsStatus() {
        return status != null
                && eventName == null
                && description == null
                && dateToStart == null
                && timeToStart == null
                && ticketClasses == null;
    }
}
