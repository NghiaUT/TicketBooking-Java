package com.nghiatr.ticket_booking.seat.repository;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.seat.entity.Seat;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.shared.jobs.expired_seats.ExpiredSeatProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    /**
     * Xóa toàn bộ các ghế thuộc về sự kiện chỉ định.
     *
     * @param event thực thể sự kiện cần xóa ghế
     */
    void deleteAllByEventId(Event event);

    /**
     * Tìm danh sách các ghế dựa trên danh sách ID ghế chỉ định.
     *
     * @param seatIds danh sách ID các ghế cần tìm
     * @return danh sách các thực thể Seat tìm thấy
     */
    List<Seat> findBySeatIdIn(List<UUID> seatIds);

    /**
     * Cập nhật trạng thái và thời gian giữ chỗ của các ghế đang ở trạng thái PENDING.
     *
     * @param status trạng thái mới của ghế
     * @param holdExpiredAt thời điểm hết hạn giữ chỗ mới
     * @param order đơn hàng giữ ghế
     * @param seatIds danh sách ID các ghế cần cập nhật
     * @return số lượng ghế được cập nhật trạng thái thành công
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Seat s
        SET s.status = :status, s.holdExpiredAt = :holdExpiredAt, s.order = :order
        WHERE s.seatId IN :seatIds
          AND s.status = 'PENDING'
        """)
    int updateSeatStatus(@Param("status") SeatStatus status,
                         @Param("holdExpiredAt") LocalDateTime holdExpiredAt,
                         @Param("order") Order order,
                         @Param("seatIds") List<UUID> seatIds);

    /**
     * Tìm các ghế không còn AVAILABLE và không thuộc đơn hàng đang xử lý.
     * Dùng để xác định chính xác các ghế đã bị người dùng khác giữ chỗ khi thao tác CAS thất bại.
     *
     * @param seatIds danh sách ID ghế kiểm tra
     * @param availableStatus trạng thái AVAILABLE để loại trừ
     * @param orderId ID đơn hàng hiện tại
     * @return danh sách các ghế không khả dụng bị giữ bởi đơn hàng khác
     */
    @Query("""
        SELECT s FROM Seat s
        WHERE s.seatId IN :seatIds
          AND s.status <> :availableStatus
          AND (s.order IS NULL OR s.order.orderId <> :orderId)
        """)
    List<Seat> findUnavailableSeatsExcludingOrder(
            @Param("seatIds") List<UUID> seatIds,
            @Param("availableStatus") SeatStatus availableStatus,
            @Param("orderId") UUID orderId
    );

    /**
     * Phân trang tìm các ghế đã hết hạn giữ chỗ phục vụ cho tác vụ giải phóng định kỳ.
     *
     * @param seatStatus trạng thái ghế cần quét (ví dụ PENDING)
     * @param now thời điểm hiện tại để kiểm tra hết hạn
     * @param pageable thông tin phân trang
     * @return trang kết quả chứa các hình chiếu ghế hết hạn ExpiredSeatProjection
     */
    Page<ExpiredSeatProjection> findByStatusAndHoldExpiredAtBefore(SeatStatus seatStatus, LocalDateTime now, Pageable pageable);

    /**
     * Tìm toàn bộ danh sách ghế liên kết với một đơn hàng.
     *
     * @param order thực thể đơn hàng
     * @return danh sách ghế thuộc đơn hàng
     */
    List<Seat> findByOrder(Order order);

    /**
     * Cập nhật trạng thái cho toàn bộ ghế thuộc một đơn hàng cụ thể.
     *
     * @param status trạng thái mới cần cập nhật (ví dụ BOOKED)
     * @param order thực thể đơn hàng sở hữu ghế
     * @return số lượng ghế được cập nhật thành công
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Seat s
        SET s.status = :status
        WHERE s.order = :order
    """)
    int updateSeatStatusByOrder(@Param("status") SeatStatus status, @Param("order") Order order);

    /**
     * Giải phóng toàn bộ ghế thuộc một đơn hàng về trạng thái chỉ định và xóa liên kết đơn hàng.
     *
     * @param status trạng thái giải phóng (ví dụ AVAILABLE)
     * @param order thực thể đơn hàng cần nhả ghế
     * @return số lượng ghế được giải phóng thành công
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Seat s
        SET s.status = :status, s.order = null, s.holdExpiredAt = null
        WHERE s.order = :order
    """)
    int releaseSeatsByOrder(@Param("status") SeatStatus status, @Param("order") Order order);
}
