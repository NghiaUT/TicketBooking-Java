package com.nghiatr.ticket_booking.payment.repository;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.payment.entity.Payment;
import com.nghiatr.ticket_booking.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    /**
     * Tìm kiếm thông tin thanh toán theo mã giao dịch nội bộ.
     *
     * @param transactionCode mã giao dịch nội bộ
     * @return Optional chứa thực thể Payment nếu tìm thấy, ngược lại Optional rỗng
     */
    Optional<Payment> findByTransactionCode(String transactionCode);

    /**
     * Tìm toàn bộ các bản ghi thanh toán thuộc về một đơn hàng.
     *
     * @param order thực thể đơn hàng cần tra cứu
     * @return danh sách các giao dịch thanh toán của đơn hàng
     */
    List<Payment> findAllByOrder(Order order);

    /**
     * Tìm bản ghi thanh toán mới nhất của một đơn hàng theo thời gian tạo giảm dần.
     *
     * @param order thực thể đơn hàng cần tra cứu
     * @return Optional chứa thực thể Payment mới nhất nếu có
     */
    Optional<Payment> findTopByOrderOrderByCreatedAtDesc(Order order);

    /**
     * Tìm kiếm bản ghi thanh toán theo khóa idempotency.
     *
     * @param idempotencyKey khóa duy nhất chống trùng lặp yêu cầu
     * @return Optional chứa thực thể Payment nếu tìm thấy
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Kiểm tra xem đơn hàng đã có bản ghi thanh toán ở trạng thái chỉ định hay chưa.
     *
     * @param order thực thể đơn hàng cần kiểm tra
     * @param status trạng thái thanh toán cần kiểm tra
     * @return true nếu tồn tại bản ghi thỏa mãn, ngược lại false
     */
    boolean existsByOrderAndStatus(Order order, PaymentStatus status);
}
