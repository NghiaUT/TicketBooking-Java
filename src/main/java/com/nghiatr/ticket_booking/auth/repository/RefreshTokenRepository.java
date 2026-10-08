package com.nghiatr.ticket_booking.auth.repository;

import com.nghiatr.ticket_booking.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    /**
     * Tìm kiếm refresh token theo chuỗi token bí mật.
     *
     * @param token chuỗi refresh token
     * @return Optional chứa RefreshToken nếu tìm thấy, ngược lại Optional rỗng
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Thu hồi toàn bộ refresh token thuộc về một người dùng.
     *
     * @param userId định danh duy nhất của người dùng
     */
    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.userId = :userId")
    void revokeAllByUserId(UUID userId);

    /**
     * Xóa toàn bộ refresh token thuộc về một người dùng khỏi cơ sở dữ liệu.
     *
     * @param userId định danh duy nhất của người dùng
     */
    void deleteByUserId(UUID userId);
}
