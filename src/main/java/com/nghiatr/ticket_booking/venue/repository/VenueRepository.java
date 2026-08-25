package com.nghiatr.ticket_booking.venue.repository;

import com.nghiatr.ticket_booking.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
}
