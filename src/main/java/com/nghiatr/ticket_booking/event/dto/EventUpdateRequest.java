package com.nghiatr.ticket_booking.event.dto;

import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.ticketClass.dto.TicketClassItem;

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
        List<TicketClassItem> ticketClasses
) {
    /**
     * Kiểm tra xem yêu cầu cập nhật có phải chỉ chứa duy nhất trường trạng thái (status) hay không.
     *
     * @return true nếu chỉ có trường status khác null và các trường khác đều null, ngược lại false
     */
    public boolean onlyContainsStatus() {
        return status != null
                && eventName == null
                && description == null
                && dateToStart == null
                && timeToStart == null
                && ticketClasses == null;
    }
}
