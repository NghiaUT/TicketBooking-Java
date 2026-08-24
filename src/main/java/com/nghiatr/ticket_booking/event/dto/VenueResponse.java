package com.nghiatr.ticket_booking.event.dto;

import com.nghiatr.ticket_booking.event.entity.Venue;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {
    private List<Venue> venues;
}
