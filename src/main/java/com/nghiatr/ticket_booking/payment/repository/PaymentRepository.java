package com.nghiatr.ticket_booking.payment.repository;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.payment.entity.Payment;
import com.nghiatr.ticket_booking.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByTransactionCode(String transactionCode);

    List<Payment> findAllByOrder(Order order);

    Optional<Payment> findTopByOrderOrderByCreatedAtDesc(Order order);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderAndStatus(Order order, PaymentStatus status);
}
