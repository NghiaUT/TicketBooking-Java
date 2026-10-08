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

    private void broadcastToEventTopic(UUID eventId, SeatStatusEvent event) {
        String destination = "/topic/events/" + eventId + "/seats";
        log.info("[WebSocket] Broadcasting {} for {} seats to destination: {}",
                event.getAction(), event.getSeatIds().size(), destination);
        messagingTemplate.convertAndSend(destination, event);
    }
}
