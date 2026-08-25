package com.nghiatr.ticket_booking.venue.entity;

import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "venues")
@Entity
public class Venue {
    @Id
    @Column(name = "venue_id", columnDefinition = "uuid")
    private UUID venueId;

    @Column(name = "venue_name", nullable = false)
    private String venueName;

    @Column
    private String address;

    @Column
    private int capacity;

    @Column(name = "map_image_url")
    private String mapImageUrl;

    @Column
    @Enumerated(EnumType.STRING)
    private SeatStatus status;
}
