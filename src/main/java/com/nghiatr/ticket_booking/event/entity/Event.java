package com.nghiatr.ticket_booking.event.entity;

import com.nghiatr.ticket_booking.event.entity.seat_layout.SeatLayout;
import com.nghiatr.ticket_booking.user.model.Admin;
import com.nghiatr.ticket_booking.user.model.Organizer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organizer_id",
            referencedColumnName = "user_id"
    )
    private Organizer organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "venue_id",
            referencedColumnName = "venue_id"
    )
    private Venue venue;

    @Column(name = "genre")
    private String genre;

    @Column(name = "event_img_url")
    private String eventImgUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "approved_by",
            referencedColumnName = "user_id"
    )
    private Admin admin;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.PENDING;

    @Column(name = "time_to_start")
    private LocalDateTime timeToStart;

    @Column(name = "date_to_start")
    private LocalDateTime dateToStart;

    @Column(name = "time_to_release")
    private LocalDateTime timeToRelease;

    @Column(name = "duration")
    private String duration;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "seat_layout_map", columnDefinition = "json")
    private SeatLayout seatLayoutMap;

    @Column(name = "reject_reason")
    private String rejectReason;

}
