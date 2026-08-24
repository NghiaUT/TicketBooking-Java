package com.nghiatr.ticket_booking.user.model;

import com.nghiatr.ticket_booking.event.entity.Event;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "organizers")
@AllArgsConstructor
@Builder
public class Organizer {
    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "business_license_url")
    private String businessLicenseUrl;

    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @Column(name = "reject_reason")
    private String rejectReason;

    @OneToMany(mappedBy = "organizer")
    private List<Event> events;

//    @OneToMany(mappedBy = "organizer")
//    private List<OrganizerBankAccount> bankAccounts;
//
//    @OneToMany(mappedBy = "organizer")
//    private List<PayLog> payLogs;
}
