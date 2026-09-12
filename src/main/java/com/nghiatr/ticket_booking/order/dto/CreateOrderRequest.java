package com.nghiatr.ticket_booking.order.dto;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        List<UUID> seatIds
) {
}
