package com.nghiatr.ticket_booking.event.dto;

import java.util.UUID;

public record TicketClassUpdateRequest(
        UUID ticketClassId,
        double price,
        int quota
) {
}
