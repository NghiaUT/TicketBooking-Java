package com.nghiatr.ticket_booking.auth.repository;

import com.nghiatr.ticket_booking.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.userId = :userId")
    void revokeAllByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
