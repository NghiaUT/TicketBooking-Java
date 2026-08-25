package com.nghiatr.ticket_booking.seat.entity;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seat_id", columnDefinition = "uuid")
    private UUID seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "event_id",
            referencedColumnName = "event_id"
    )
    private Event eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ticket_class_id",
            referencedColumnName = "ticket_class_id"
    )
    private TicketClass ticketClassId;

    @Column
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Column(name = "hold_expired_at")
    private LocalDateTime holdExpiredAt;
}
