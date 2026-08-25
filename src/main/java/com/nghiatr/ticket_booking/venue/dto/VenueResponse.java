package com.nghiatr.ticket_booking.venue.dto;

import com.nghiatr.ticket_booking.venue.entity.Venue;
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
