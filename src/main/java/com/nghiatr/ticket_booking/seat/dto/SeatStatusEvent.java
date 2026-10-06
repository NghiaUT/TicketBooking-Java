package com.nghiatr.ticket_booking.seat.dto;

import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusEvent {
    public enum ActionType {
        LOCKED,   // Ghế vừa được giữ
        RELEASED, // Ghế vừa được nhả
        BOOKED    // Ghế đã được thanh toán thàng công.
    }

    private UUID eventId;
    private List<UUID> seatIds;
    private SeatStatus status;      // AVAILABLE, PENDING, BOOKED
    private ActionType action;      // LOCKED, RELEASED, BOOKED
    private LocalDateTime expiredAt; // Thời điểm hết hạn giữ
    private LocalDateTime timestamp;
}
