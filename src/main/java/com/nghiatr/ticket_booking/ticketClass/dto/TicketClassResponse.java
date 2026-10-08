package com.nghiatr.ticket_booking.ticketClass.dto;

import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClassType;

import java.util.UUID;

public record TicketClassResponse(
        UUID ticketClassId,
        UUID eventId,
        String className,
        double price,
        int quota,
        TicketClassType type,
        String color,
        String description
) {
    /**
     * Chuyển đổi từ thực thể TicketClass sang đối tượng TicketClassResponse.
     *
     * @param ticketClass thực thể hạng vé
     * @return đối tượng TicketClassResponse
     */
    public static TicketClassResponse toResponse(TicketClass ticketClass) {
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