package com.nghiatr.ticket_booking.user.repository;

import com.nghiatr.ticket_booking.user.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
