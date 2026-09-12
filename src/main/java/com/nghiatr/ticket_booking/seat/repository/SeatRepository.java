package com.nghiatr.ticket_booking.seat.repository;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.seat.entity.Seat;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.shared.jobs.expired_seats.ExpiredSeatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    void deleteAllByEventId(Event event);
    List<Seat> findBySeatIdIn(List<UUID> seatIds);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Seat s SET s.status = :status, s.holdExpiredAt = :holdExpiredAt, s.order = :order WHERE s.seatId IN :seatIds")
    int updateSeatStatus(@Param("status") SeatStatus status,
                         @Param("holdExpiredAt") LocalDateTime holdExpiredAt,
                         @Param("order") Order order,
                         @Param("seatIds") List<UUID> seatIds);

    List<ExpiredSeatProjection> findByStatusAndHoldExpiredAtBefore(SeatStatus seatStatus, LocalDateTime now);
}
