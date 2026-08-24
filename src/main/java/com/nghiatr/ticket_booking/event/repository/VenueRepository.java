package com.nghiatr.ticket_booking.event.repository;

import com.nghiatr.ticket_booking.event.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {

}
