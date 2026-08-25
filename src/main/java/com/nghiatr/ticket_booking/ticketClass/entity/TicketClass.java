package com.nghiatr.ticket_booking.ticketClass.entity;

import com.nghiatr.ticket_booking.event.entity.Event;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_classes")
public class TicketClass {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ticket_class_id", columnDefinition = "uuid")
    private UUID ticketClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "event_id",
            referencedColumnName = "event_id"
    )
    private Event eventId;

    @Column(name = "class_name")
    private String className;

    @Column(nullable = false)
    private double price;

    @Column
    private int quota;

    @Enumerated(EnumType.STRING)
    private TicketClassType type;

    @Column
    private String color;

    @Column
    private String description;
}
