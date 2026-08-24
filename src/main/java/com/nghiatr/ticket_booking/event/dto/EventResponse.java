package com.nghiatr.ticket_booking.event.dto;

import com.nghiatr.ticket_booking.event.entity.Event;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private List<EventItemResponse> events;
}
