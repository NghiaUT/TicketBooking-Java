package com.nghiatr.ticket_booking.event.service;

import com.nghiatr.ticket_booking.event.dto.CreateEventRequest;
import com.nghiatr.ticket_booking.event.dto.EventItemResponse;
import com.nghiatr.ticket_booking.event.dto.EventResponse;
import com.nghiatr.ticket_booking.event.dto.EventUpdateRequest;
import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.event.exception.EventErrorCode;
import com.nghiatr.ticket_booking.event.repository.EventRepository;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.shared.utils.SecurityUtils;
import com.nghiatr.ticket_booking.user.model.Organizer;
import com.nghiatr.ticket_booking.user.repository.OrganizerRepository;
import com.nghiatr.ticket_booking.venue.entity.Venue;
import com.nghiatr.ticket_booking.venue.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final VenueService venueService;
    private final SecurityUtils securityUtils;

    //Helper để lấy các event thuộc về organizer.
    public Event getOrganizerEvent(UUID eventId) {
        UUID organizerId = securityUtils.getCurrentUserId();

        return eventRepository
                .findByEventIdAndOrganizer_UserId(eventId, organizerId)
                .orElseThrow(() ->
                        new AppException(EventErrorCode.EVENT_NOT_FOUND)
                );
    }

    public Event getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
    }

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public EventResponse findAllEvent() {
        List<EventItemResponse> events = eventRepository
                .findAllByStatus(EventStatus.APPROVED)
                .stream()
                .map(EventItemResponse::from)
                .toList();

        return EventResponse.builder()
                .events(events)
                .build();
    }

    public EventItemResponse findEventById(UUID eventId) {
        return eventRepository.
                findById(eventId)
                .map(EventItemResponse::from)
                .orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
    }

    public EventResponse findOrganizerEvent(UUID organizerId) {
        return EventResponse.builder()
                .events(
                        eventRepository.findAllByOrganizer_UserId(organizerId)
                                .stream()
                                .map(EventItemResponse::from)
                                .toList()
                )
                .build();
    }

    public EventItemResponse createBasicInfo(UUID organizerId, CreateEventRequest eventData) {
        LocalDateTime timeToStart = eventData.getTimeToStart();
        LocalDateTime timeToRelease = eventData.getTimeToRelease();

        if (timeToStart.isBefore(timeToRelease)
                || timeToStart.isBefore(LocalDateTime.now())
                || timeToRelease.isBefore(LocalDateTime.now())
        ) {
            throw new AppException(EventErrorCode.INVALID_EVENT_TIME);
        }

        Venue venue = venueService.getVenueById(eventData.getVenueId());

        Organizer organizer = organizerRepository.findByUserId(organizerId)
                .orElseThrow(() -> new AppException(EventErrorCode.ORGANIZER_NOT_FOUND));

        Event newEvent = Event.builder()
                .eventName(eventData.getEventName())
                .venue(venue)
                .organizer(organizer)
                .eventImgUrl(eventData.getEventImgUrl())
                .dateToStart(eventData.getDateToStart())
                .timeToRelease(eventData.getTimeToRelease())
                .timeToStart(eventData.getTimeToStart())
                .description(eventData.getDescription())
                .genre(eventData.getGenre())
                .duration(eventData.getDuration())
                .status(EventStatus.PENDING)
                .build();
        eventRepository.save(newEvent);

        return EventItemResponse.from(newEvent);
    }

    // ======== Cập nhật các field khác của Event.
    public void updateEventFields(
            Event event,
            EventUpdateRequest request
    ) {
        if (request.eventName() != null) {
            event.setEventName(request.eventName());
        }

        if (request.description() != null) {
            event.setDescription(request.description());
        }

        if (request.status() != null) {
            event.setStatus(request.status());
        }

        if (request.dateToStart() != null) {
            event.setDateToStart(request.dateToStart());
        }

        if (request.timeToStart() != null) {
            event.setTimeToStart(request.timeToStart());
        }
    }
}
