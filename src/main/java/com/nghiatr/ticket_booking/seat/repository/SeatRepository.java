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

    /**
     * Release expired held seats back to AVAILABLE.
     * Điều kiện "s.status = 'PENDING'" là safety net: ngăn release nhầm ghế đã chuyển
     * sang BOOKED bởi một transaction khác trong khoảng thời gian giữa lần đọc (find)
     * và lần ghi (update) của CronJob.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Seat s
        SET s.status = :status, s.holdExpiredAt = :holdExpiredAt, s.order = :order
        WHERE s.seatId IN :seatIds
          AND s.status = 'PENDING'
        """)
    int updateSeatStatus(@Param("status") SeatStatus status,
                         @Param("holdExpiredAt") LocalDateTime holdExpiredAt,
                         @Param("order") Order order,
                         @Param("seatIds") List<UUID> seatIds);

    /**
     * Tìm các ghế KHÔNG còn AVAILABLE và KHÔNG thuộc order đang xử lý.
     * Dùng để báo lỗi chính xác sau khi CAS update thất bại một phần:
     * phân biệt ghế mình đã lock thành công (của mình) với ghế bị người khác giữ.
     */
    @Query("""
        SELECT s FROM Seat s
        WHERE s.seatId IN :seatIds
          AND s.status <> :availableStatus
          AND (s.order IS NULL OR s.order.orderId <> :orderId)
        """)
    List<Seat> findUnavailableSeatsExcludingOrder(
            @Param("seatIds") List<UUID> seatIds,
            @Param("availableStatus") SeatStatus availableStatus,
            @Param("orderId") UUID orderId
    );

    List<ExpiredSeatProjection> findByStatusAndHoldExpiredAtBefore(SeatStatus seatStatus, LocalDateTime now);
}
