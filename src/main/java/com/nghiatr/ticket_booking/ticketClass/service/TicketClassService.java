package com.nghiatr.ticket_booking.ticketClass.service;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.exception.EventErrorCode;
import com.nghiatr.ticket_booking.event.repository.EventRepository;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.ticketClass.dto.TicketClassItem;
import com.nghiatr.ticket_booking.ticketClass.dto.TicketClassResponse;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import com.nghiatr.ticket_booking.ticketClass.exception.TicketClassErrorCode;
import com.nghiatr.ticket_booking.ticketClass.repository.TicketClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketClassService {
    private final TicketClassRepository ticketClassRepository;
    private final EventRepository eventRepository;

    private boolean isUUID(String id) {
        return id != null && id.matches(
                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
        );
    }

    public int createTicketClasses(UUID eventId, List<TicketClassItem> ticketClassItemData) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));

        List<TicketClass> ticketClasses = ticketClassItemData.stream()
                .map(ticket -> TicketClass.builder()
                        .eventId(event)
                        .className(ticket.getClassName())
                        .price(ticket.getPrice())
                        .quota(ticket.getQuota())
                        .type(ticket.getType())
                        .color(ticket.getColor())
                        .description(ticket.getDescription())
                        .build())
                .toList();

        return ticketClassRepository.saveAll(ticketClasses).size();
    }

    @Transactional
    public int editTicketClasses(UUID eventId, List<TicketClassItem> ticketClassItemData) {
        // 1. Xóa những ticket class bị xóa trên frontend:

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));

        List<TicketClass> existingTickets =
                ticketClassRepository.findAllByEventId(event);

        // 2. Lấy ID các ticket class frontend vẫn còn giữ
        Set<String> incomingIds = ticketClassItemData.stream()
                .map(TicketClassItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. Những ID DB không xuất hiện trong request => đã bị xóa
        List<TicketClass> deletedTickets = existingTickets.stream()
                .filter(ticket -> !incomingIds.contains(ticket.getTicketClassId().toString()))
                .toList();

        ticketClassRepository.deleteAll(deletedTickets);

        List<TicketClass> ticketClasses = ticketClassItemData.stream()
                .map(ticket -> {
                    if(!isUUID(ticket.getId())) {
                        // Tạo mới:
                        return TicketClass.builder()
                                .eventId(event)
                                .className(ticket.getClassName())
                                .price(ticket.getPrice())
                                .quota(ticket.getQuota())
                                .type(ticket.getType())
                                .color(ticket.getColor())
                                .description(ticket.getDescription())
                                .build();
                    }

                    // Vé cũ
                    TicketClass ticketClass = ticketClassRepository.findById(UUID.fromString(ticket.getId()))
                            .orElseThrow(() -> new AppException(TicketClassErrorCode.TICKET_CLASS_NOT_FOUND));

                    ticketClass.setClassName(ticket.getClassName());
                    ticketClass.setPrice(ticket.getPrice());
                    ticketClass.setQuota(ticket.getQuota());
                    ticketClass.setType(ticket.getType());
                    ticketClass.setColor(ticket.getColor());
                    ticketClass.setDescription(ticket.getDescription());

                    return ticketClass;
                })
                .toList();

        return ticketClassRepository.saveAll(ticketClasses).size();
    }

    public List<TicketClassResponse> getByEventId(UUID eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));

        List<TicketClass> ticketClasses = ticketClassRepository.findAllByEventId(event);

        return ticketClasses.stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketClassResponse toResponse(TicketClass ticketClass) {
        return new TicketClassResponse(
                ticketClass.getTicketClassId(),
                ticketClass.getEventId().getEventId(),
                ticketClass.getClassName(),
                ticketClass.getPrice(),
                ticketClass.getQuota(),
                ticketClass.getType(),
                ticketClass.getColor(),
                ticketClass.getDescription()
        );
    }
}
