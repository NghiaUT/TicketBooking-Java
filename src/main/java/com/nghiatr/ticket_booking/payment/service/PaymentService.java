package com.nghiatr.ticket_booking.payment.service;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.order.entity.OrderStatus;
import com.nghiatr.ticket_booking.order.repository.OrderRepository;
import com.nghiatr.ticket_booking.payment.dto.CreatePaymentRequest;
import com.nghiatr.ticket_booking.payment.dto.PaymentCommand;
import com.nghiatr.ticket_booking.payment.dto.PaymentInitResult;
import com.nghiatr.ticket_booking.payment.dto.PaymentResponse;
import com.nghiatr.ticket_booking.payment.entity.Payment;
import com.nghiatr.ticket_booking.payment.entity.PaymentStatus;
import com.nghiatr.ticket_booking.payment.exception.PaymentErrorCode;
import com.nghiatr.ticket_booking.payment.repository.PaymentRepository;
import com.nghiatr.ticket_booking.payment.strategy.PaymentStrategy;
import com.nghiatr.ticket_booking.payment.strategy.PaymentStrategyFactory;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    /**
     * Khởi tạo giao dịch thanh toán và sinh URL thanh toán qua Strategy tương ứng.
     */
    @Transactional
    public PaymentResponse initPayment(CreatePaymentRequest request, String clientIp) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new AppException(PaymentErrorCode.ORDER_ALREADY_PAID);
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(PaymentErrorCode.ORDER_EXPIRED, "Đơn hàng đã bị hủy, không thể thanh toán");
        }

        if (order.getExpiredAt() != null && order.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(PaymentErrorCode.ORDER_EXPIRED, "Đơn hàng đã hết thời gian giữ vé");
        }

        // Tạo mã giao dịch duy nhất cho phiên thanh toán
        String transactionCode = "TXN-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(request.method())
                .transactionCode(transactionCode)
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(request.method());

        PaymentCommand command = PaymentCommand.builder()
                .paymentId(payment.getPaymentId())
                .orderId(order.getOrderId())
                .amount(order.getTotalAmount())
                .method(request.method())
                .transactionCode(transactionCode)
                .description("Thanh toan ve su kien - Don hang #" + order.getOrderId())
                .customerEmail(order.getCustomerEmail())
                .customerName(order.getCustomerName())
                .ipAddress(clientIp)
                .returnUrl(request.returnUrl())
                .build();

        PaymentInitResult initResult = strategy.initPayment(command);

        return PaymentResponse.from(payment, initResult.paymentUrl());
    }

    /**
     * Đánh dấu thanh toán thành công (Bảo đảm Idempotency: nếu đã SUCCESS thì trả về ngay).
     */
    @Transactional
    public Payment markSuccess(String transactionCode, String gatewayTransactionId) {
        Payment payment = getByTransactionCode(transactionCode);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("[Payment] Transaction {} da o trang thai SUCCESS truoc do (Idempotent bypass)", transactionCode);
            return payment;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayTransactionId(gatewayTransactionId);
        return paymentRepository.save(payment);
    }

    /**
     * Đánh dấu thanh toán thất bại.
     */
    @Transactional
    public Payment markFailed(String transactionCode, String gatewayTransactionId) {
        Payment payment = getByTransactionCode(transactionCode);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("[Payment] Transaction {} da SUCCESS, khong the chuyen sang FAILED", transactionCode);
            return payment;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setGatewayTransactionId(gatewayTransactionId);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getByTransactionCode(String transactionCode) {
        return paymentRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
