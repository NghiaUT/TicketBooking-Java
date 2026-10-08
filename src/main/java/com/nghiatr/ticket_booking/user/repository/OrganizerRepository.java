package com.nghiatr.ticket_booking.user.repository;

import com.nghiatr.ticket_booking.user.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizerRepository extends JpaRepository<Organizer, UUID> {
    /**
     * Tìm thông tin ban tổ chức dựa trên mã định danh người dùng.
     *
     * @param id định danh người dùng của ban tổ chức
     * @return Optional chứa thực thể Organizer nếu tìm thấy, ngược lại Optional rỗng
     */
    Optional<Organizer> findByUserId(UUID id);
}
