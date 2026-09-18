package com.nghiatr.ticket_booking.order.repository;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.order.entity.OrderStatus;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.user.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByCustomer(Customer customer);

    @Query("""
    SELECT o.orderId
    FROM Order o
    WHERE o.status = :status
      AND o.expiredAt < :now
""")
    List<UUID> findIdsByStatusAndExpiredAtBefore(
            @Param("status") OrderStatus status,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
    UPDATE Order o
    SET o.status = :status
    WHERE o.orderId IN :orderIds
""")
    int cancelOrders(
            @Param("orderIds") List<UUID> orderIds,
            @Param("status") OrderStatus status
    );

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE Seat s 
    SET s.status = :newStatus, 
        s.order = :order, 
        s.holdExpiredAt = :expiredAt 
    WHERE s.seatId IN :seatIds 
      AND s.status = 'AVAILABLE'
    """)
    int updateSeatStatusIfAvailable(
            @Param("newStatus") SeatStatus newStatus,
            @Param("order") Order order,
            @Param("expiredAt") LocalDateTime expiredAt,
            @Param("seatIds") List<UUID> seatIds
    );
}
