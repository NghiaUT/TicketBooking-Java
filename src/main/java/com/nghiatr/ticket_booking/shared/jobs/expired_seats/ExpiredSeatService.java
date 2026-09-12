package com.nghiatr.ticket_booking.shared.jobs.expired_seats;

import com.nghiatr.ticket_booking.order.entity.OrderStatus;
import com.nghiatr.ticket_booking.order.repository.OrderRepository;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.seat.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpiredSeatService {
    private final SeatRepository seatRepository;
    private final OrderRepository orderRepository;
//    private final PaymentRepository paymentRepository;

    @Transactional
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now();

        List<ExpiredSeatProjection> expiredSeats =
                seatRepository.findByStatusAndHoldExpiredAtBefore(
                        SeatStatus.PENDING,
                        now
                );
        if (expiredSeats.isEmpty()) {
            System.out.println("[CronJob] No expired seats found.");
            return;
        }

        List<UUID> expiredSeatIds = expiredSeats.stream()
                .map(ExpiredSeatProjection::getSeatId)
                .toList();

        // 2. Release the seats. (bulk update with SQL)
        seatRepository.updateSeatStatus(SeatStatus.AVAILABLE, null, null, expiredSeatIds);

        // 3. Find PENDING Orders out of date.
        List<UUID> expiredOrders = orderRepository.findIdsByStatusAndExpiredAtBefore(OrderStatus.PENDING, now);

        // 4. Update Order -> cancel.
        if(!expiredOrders.isEmpty()) {
            orderRepository.cancelOrders(
                    expiredOrders,
                    OrderStatus.CANCELLED
            );

            // Get payments to create a new FAILED Method.
        }

        // 5.Try Socket to broadcast seats to all users.
    }
}
