package com.nghiatr.ticket_booking.event.service;

import com.nghiatr.ticket_booking.event.dto.*;
import com.nghiatr.ticket_booking.event.dto.layout_request.CreateLayoutRequest;
import com.nghiatr.ticket_booking.event.dto.layout_request.SeatLayoutResponse;
import com.nghiatr.ticket_booking.event.entity.*;
import com.nghiatr.ticket_booking.event.entity.seat_layout.SeatBlock;
import com.nghiatr.ticket_booking.event.entity.seat_layout.SeatLayout;
import com.nghiatr.ticket_booking.event.exception.EventErrorCode;
import com.nghiatr.ticket_booking.event.repository.EventRepository;
import com.nghiatr.ticket_booking.user.repository.OrganizerRepository;
import com.nghiatr.ticket_booking.event.repository.SeatRepository;
import com.nghiatr.ticket_booking.event.repository.VenueRepository;
import com.nghiatr.ticket_booking.event.validation.EventActionValidator;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.shared.utils.SecurityUtils;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import com.nghiatr.ticket_booking.ticketClass.exception.TicketClassErrorCode;
import com.nghiatr.ticket_booking.ticketClass.repository.TicketClassRepository;
import com.nghiatr.ticket_booking.user.model.Organizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final OrganizerRepository organizerRepository;
    private final SeatRepository seatRepository;
    private final TicketClassRepository ticketClassRepository;
    private final EventActionValidator eventActionValidator;
    private final SecurityUtils securityUtils;

    //Helper để lấy các event thuộc về organizer.
    private Event getOrganizerEvent(UUID eventId) {
        UUID organizerId = securityUtils.getCurrentUserId();

        return eventRepository
                .findByEventIdAndOrganizer_UserId(eventId, organizerId)
                .orElseThrow(() ->
                        new AppException(EventErrorCode.EVENT_NOT_FOUND)
                );
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

    public VenueResponse findAllVenues() {
        List<Venue> venues = venueRepository.findAll();

        return VenueResponse.builder()
                .venues(venues)
                .build();
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

        Venue venue = venueRepository.findById(eventData.getVenueId())
                .orElseThrow(() -> new AppException(EventErrorCode.VENUE_NOT_FOUND));

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

    @Transactional
    public SeatLayoutResponse createLayout(
            UUID eventId,
            CreateLayoutRequest request
    ) {
        // BƯỚC 1: Kiểm tra Event
        Event event = getOrganizerEvent(eventId);

        // BƯỚC 2: Lưu layout JSON vào Event
        // Hủy tất cả ghế trước đó của event.
        seatRepository.deleteAllByEventId(event);
        // Tạo mới object seatLayout để lưu vào Event.
        SeatLayout seatLayout = request.toSeatLayout();
        event.setSeatLayoutMap(seatLayout);
        eventRepository.save(event);

        List<Seat> seats = new ArrayList<>();

        for(SeatBlock block : seatLayout.getSeatLayout()) {
            if(block.getRows() == 0 || block.getCols() == 0) {
                continue;
            }

            TicketClass ticketClass = ticketClassRepository.findById(block.getTicketClassId())
                    .orElseThrow(() -> new AppException(TicketClassErrorCode.TICKET_CLASS_NOT_FOUND));

            Set<String> deletedSeats = block.getDeletedSeats()
                    .stream()
                    .map(seat ->
                            seat.getRow() + "-" + seat.getCol())
                    .collect(Collectors.toSet());

            for(int row = 1; row <= block.getRows(); row++) {
                for(int col = 1; col <= block.getCols(); col++) {

                    String coordinate = row + "-" + col;

                    if(deletedSeats.contains(coordinate)) {
                        continue;
                    }

                    Seat seat = Seat.builder()
                            .eventId(event)
                            .ticketClassId(ticketClass)
                            .name(
                                    block.getBlockId()
                                    + "-R" + row
                                    + "-L" + col
                            )
                            .status(SeatStatus.AVAILABLE)
                            .build();
                    seats.add(seat);
                }
            }
        }

        // Bước 4: Insert hàng loạt ghế vào db
        if(!seats.isEmpty()) {
            seatRepository.saveAll(seats);
        }

        return new SeatLayoutResponse(seats.size());
    }

    @Transactional
    public EventItemResponse update(
            UUID eventId,
            EventUpdateRequest updateData
    ) {
        Event event = getOrganizerEvent(eventId);
        List<TicketClass> ticketClasses = ticketClassRepository.findAllByEventId(event);

        eventActionValidator.validate(
                event,
                ticketClasses,
                updateData,
                EventActionType.UPDATE
        );

        if(updateData.ticketClasses() != null
            && !updateData.ticketClasses().isEmpty()) {
            updateTicketClasses(updateData.ticketClasses());
        }

        updateEventFields(event, updateData);

        return EventItemResponse.from(event);
    }

    // ========= Cập nhật ticket Class hiện có của một event.
    private void updateTicketClasses(
            List<TicketClassUpdateRequest> requests
    ) {
        List<TicketClass> updatedTicketClasses = new ArrayList<>();
        for(TicketClassUpdateRequest request : requests) {

            TicketClass ticketClass = ticketClassRepository
                    .findById(request.ticketClassId())
                    .orElseThrow(() ->
                            new AppException(EventErrorCode.INVALID_TICKET_CLASS)
                    );

            if(request.price() != 0.0) {
                ticketClass.setPrice(request.price());
            }

            if(request.quota() != 0) {
                ticketClass.setQuota(request.quota());
            }

            updatedTicketClasses.add(ticketClass);
        }

        ticketClassRepository.saveAll(updatedTicketClasses);
    }

    // ======== Cập nhật các field khác của Event.
    private void updateEventFields(
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
