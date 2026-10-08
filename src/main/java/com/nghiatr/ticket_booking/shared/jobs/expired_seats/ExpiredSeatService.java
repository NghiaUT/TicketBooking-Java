package com.nghiatr.ticket_booking.shared.jobs.expired_seats;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.order.entity.OrderStatus;
import com.nghiatr.ticket_booking.order.repository.OrderRepository;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.seat.repository.SeatRepository;
import com.nghiatr.ticket_booking.seat.service.SeatWebSocketPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiredSeatService {

    private final static int BATCH_SIZE = 100;

    private final SeatRepository seatRepository;
    private final OrderRepository orderRepository;
    private final SeatWebSocketPublisher seatWebSocketPublisher;
//    private final PaymentRepository paymentRepository;

    /**
     * Quét và giải phóng các ghế đã hết hạn giữ chỗ (PENDING), hủy các đơn hàng tương ứng và phát sự kiện WebSocket.
     */
    @Transactional
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now();

        while(true) {
            Page<ExpiredSeatProjection> expiredSeats =
                    seatRepository.findByStatusAndHoldExpiredAtBefore(
                            SeatStatus.PENDING,
                            now,
                            (Pageable) PageRequest.of(0,  BATCH_SIZE)
                    );

            if (expiredSeats.isEmpty()) {
                log.info("[CronJob] No expired seats found.");
                break;
            }

            List<UUID> expiredSeatIds = expiredSeats.stream()
                    .map(ExpiredSeatProjection::getSeatId)
                    .toList();

            // 2. Release the seats. (bulk update with SQL)
            seatRepository.updateSeatStatus(SeatStatus.AVAILABLE, null, null, expiredSeatIds);

            // 3. Find PENDING Orders out of date.
            List<UUID> expiredOrders = orderRepository.findIdsByStatusAndExpiredAtBefore(OrderStatus.PENDING, now);

            // 4. Update Order -> cancel.
            if (!expiredOrders.isEmpty()) {
                orderRepository.cancelOrders(
                        expiredOrders,
                        OrderStatus.CANCELLED
                );

                // Get payments to create a new FAILED Method.
            }

            // 5.Try Socket to broadcast seats to all users.
            Map<Event, List<UUID>> seatsByEvent = expiredSeats.stream()
                    .collect(Collectors.groupingBy(
                            ExpiredSeatProjection::getEventId,
                            Collectors.mapping(ExpiredSeatProjection::getSeatId, Collectors.toList())
                    ));

            seatsByEvent.forEach((event, seatIdList) -> {
                seatWebSocketPublisher.publishSeatReleased(event.getEventId(), seatIdList);
            });
        }
    }
}
