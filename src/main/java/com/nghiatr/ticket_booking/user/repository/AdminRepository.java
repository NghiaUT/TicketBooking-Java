package com.nghiatr.ticket_booking.user.repository;

import com.nghiatr.ticket_booking.user.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
}
