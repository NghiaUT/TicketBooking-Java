package com.nghiatr.ticket_booking.seat.service;


import com.nghiatr.ticket_booking.seat.dto.SeatStatusEvent;
import com.nghiatr.ticket_booking.seat.entity.Seat;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatWebSocketPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    // Phát sự kiện khóa ghế (khi đặt ghế)
    /**
     * Phát sự kiện khóa ghế tạm thời (PENDING) khi khách hàng giữ chỗ qua WebSocket.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @param seatIds danh sách ID các ghế bị khóa
     * @param expiredAt thời điểm hết hạn giữ chỗ
     */
    public void publishSeatLocked(UUID eventId, List<UUID> seatIds, LocalDateTime expiredAt) {
        SeatStatusEvent event = SeatStatusEvent.builder()
                .eventId(eventId)
                .seatIds(seatIds)
                .status(SeatStatus.PENDING)
                .action(SeatStatusEvent.ActionType.LOCKED)
                .expiredAt(expiredAt)
                .timestamp(LocalDateTime.now())
                .build();

        broadcastToEventTopic(eventId, event);
    }

    // Phát sự kiện nhả khóa ghế.
    /**
     * Phát sự kiện mở khóa ghế trở lại trạng thái AVAILABLE qua WebSocket.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @param seatIds danh sách ID các ghế được mở khóa
     */
    public void publishSeatReleased(UUID eventId, List<UUID> seatIds) {
        SeatStatusEvent event = SeatStatusEvent.builder()
                .eventId(eventId)
                .seatIds(seatIds)
                .status(SeatStatus.AVAILABLE)
                .action(SeatStatusEvent.ActionType.RELEASED)
                .timestamp(LocalDateTime.now())
                .build();

        broadcastToEventTopic(eventId, event);
    }

    // Phát sự kiện ghế đã được đặt:
    /**
     * Phát sự kiện ghế đã được đặt và thanh toán thành công (BOOKED) qua WebSocket.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @param seatIds danh sách ID các ghế đã được đặt
     */
    public void publishSeatBooked(UUID eventId, List<UUID> seatIds) {
        SeatStatusEvent event = SeatStatusEvent.builder()
                .eventId(eventId)
                .seatIds(seatIds)
                .status(SeatStatus.BOOKED)
                .action(SeatStatusEvent.ActionType.BOOKED)
                .timestamp(LocalDateTime.now())
                .build();

        broadcastToEventTopic(eventId, event);
    }

    /**
     * Gửi sự kiện trạng thái ghế đến topic WebSocket tương ứng của sự kiện.
     *
     * @param eventId định danh sự kiện
     * @param event đối tượng sự kiện trạng thái ghế
     */
    private void broadcastToEventTopic(UUID eventId, SeatStatusEvent event) {
        String destination = "/topic/events/" + eventId + "/seats";
        log.info("[WebSocket] Broadcasting {} for {} seats to destination: {}",
                event.getAction(), event.getSeatIds().size(), destination);
        messagingTemplate.convertAndSend(destination, event);
    }
}
