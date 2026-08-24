package com.nghiatr.ticket_booking.ticketClass.dto;

import java.util.List;

public record TicketClassRequest(
        List<TicketClassItem> ticketClasses
) {
}
