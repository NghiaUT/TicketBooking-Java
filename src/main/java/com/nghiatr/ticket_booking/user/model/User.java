package com.nghiatr.ticket_booking.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(length = 300)
    private String name;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_profile_complete")
    private boolean isProfileComplete;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "created_at")
    private Instant createdAt;

    /**
     * Kiểm tra người dùng có vai trò cụ thể hay không.
     *
     * @param role vai trò cần đối chiếu
     * @return true nếu người dùng có vai trò tương ứng, ngược lại false
     */
    public boolean hasRole(UserRole role) {
        return this.role == role;
    }

    /**
     * Kiểm tra xem người dùng có phải là quản trị viên hệ thống (ADMIN) hay không.
     *
     * @return true nếu người dùng mang vai trò ADMIN, ngược lại false
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Admin admin;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Organizer organizer;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Customer customer;
}
