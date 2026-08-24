package com.nghiatr.ticket_booking.user.repository;

import com.nghiatr.ticket_booking.user.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizerRepository extends JpaRepository<Organizer, UUID> {
    Optional<Organizer> findByUserId(UUID id);
}
