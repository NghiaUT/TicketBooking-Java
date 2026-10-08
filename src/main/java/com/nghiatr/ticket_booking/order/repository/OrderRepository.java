package com.nghiatr.ticket_booking.order.repository;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.order.entity.OrderStatus;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.user.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    /**
     * Tìm kiếm toàn bộ đơn hàng thuộc về một khách hàng cụ thể.
     *
     * @param customer thực thể khách hàng cần tìm
     * @return danh sách các đơn hàng của khách hàng
     */
    List<Order> findAllByCustomer(Customer customer);

    /**
     * Tìm danh sách ID các đơn hàng có trạng thái chỉ định và đã hết thời gian giữ chỗ.
     *
     * @param status trạng thái của đơn hàng cần kiểm tra
     * @param now thời điểm hiện tại để so sánh hạn dùng
     * @return danh sách ID của các đơn hàng đã hết hạn
     */
    @Query("""
    SELECT o.orderId
    FROM Order o
    WHERE o.status = :status
      AND o.expiredAt < :now
""")
    List<UUID> findIdsByStatusAndExpiredAtBefore(
            @Param("status") OrderStatus status,
            @Param("now") LocalDateTime now
    );

    /**
     * Cập nhật trạng thái hủy đồng loạt cho danh sách các đơn hàng chỉ định.
     *
     * @param orderIds danh sách ID các đơn hàng cần hủy
     * @param status trạng thái mới (ví dụ CANCELLED)
     * @return số lượng đơn hàng được cập nhật thành công
     */
    @Modifying
    @Query("""
    UPDATE Order o
    SET o.status = :status
    WHERE o.orderId IN :orderIds
""")
    int cancelOrders(
            @Param("orderIds") List<UUID> orderIds,
            @Param("status") OrderStatus status
    );

    /**
     * Giữ chỗ ghế nguyên tử (CAS update): chỉ cập nhật trạng thái nếu ghế đang ở trạng thái AVAILABLE.
     *
     * @param newStatus trạng thái mới của ghế (PENDING)
     * @param order đơn hàng thực hiện giữ chỗ
     * @param expiredAt thời điểm hết hạn giữ chỗ
     * @param seatIds danh sách ID các ghế cần giữ
     * @return số lượng ghế được cập nhật trạng thái thành công
     */
    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE Seat s 
    SET s.status = :newStatus, 
        s.order = :order, 
        s.holdExpiredAt = :expiredAt 
    WHERE s.seatId IN :seatIds 
      AND s.status = 'AVAILABLE'
    """)
    int updateSeatStatusIfAvailable(
            @Param("newStatus") SeatStatus newStatus,
            @Param("order") Order order,
            @Param("expiredAt") LocalDateTime expiredAt,
            @Param("seatIds") List<UUID> seatIds
    );
}
