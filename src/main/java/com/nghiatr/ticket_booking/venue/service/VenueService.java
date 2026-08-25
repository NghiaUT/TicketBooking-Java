package com.nghiatr.ticket_booking.venue.service;

import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.venue.entity.Venue;
import com.nghiatr.ticket_booking.venue.exception.VenueErrorCode;
import com.nghiatr.ticket_booking.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;

    public Venue getVenueById(UUID venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new AppException(VenueErrorCode.VENUE_NOT_FOUND));
    }

    public Venue saveVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    public List<Venue> findAllVenues() {
        return venueRepository.findAll();
    }
}
