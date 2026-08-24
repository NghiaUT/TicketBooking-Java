package com.nghiatr.ticket_booking.event.dto;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.event.entity.seat_layout.SeatLayout;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record EventItemResponse(
        UUID eventId,
        String eventName,
        UUID organizerId,
        UUID venueId,
        String genre,
        String eventImgUrl,
        String description,
        EventStatus status,
        LocalDateTime timeToStart,
        LocalDateTime dateToStart,
        LocalDateTime timeToRelease,
        String duration,
        SeatLayout seatLayoutMap,
        String rejectReason
) {
    public static EventItemResponse from(Event event) {
        return new EventItemResponse(
                event.getEventId(),
                event.getEventName(),
                event.getOrganizer().getUserId(),
                event.getVenue().getVenueId(),
                event.getGenre(),
                event.getEventImgUrl(),
                event.getDescription(),
                event.getStatus(),
                event.getTimeToStart(),
                event.getDateToStart(),
                event.getTimeToRelease(),
                event.getDuration(),
                event.getSeatLayoutMap(),
                event.getRejectReason()
        );
    }
}