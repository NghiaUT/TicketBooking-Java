package com.nghiatr.ticket_booking.event.repository;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.user.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    /**
     * Tìm danh sách các sự kiện theo trạng thái phê duyệt.
     *
     * @param status trạng thái của sự kiện cần tìm
     * @return danh sách các sự kiện phù hợp
     */
    List<Event> findAllByStatus(EventStatus status);

    /**
     * Tìm danh sách các sự kiện thuộc quyền sở hữu của một ban tổ chức.
     *
     * @param organizerId định danh người dùng của ban tổ chức
     * @return danh sách các sự kiện do ban tổ chức tạo
     */
    List<Event> findAllByOrganizer_UserId(UUID organizerId);

    /**
     * Tìm sự kiện theo ID sự kiện và ID ban tổ chức quản lý.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @param organizerId định danh người dùng của ban tổ chức
     * @return Optional chứa thực thể Event nếu tìm thấy, ngược lại Optional rỗng
     */
    Optional<Event> findByEventIdAndOrganizer_UserId(
            UUID eventId,
            UUID organizerId
    );
}
