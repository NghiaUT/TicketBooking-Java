package com.nghiatr.ticket_booking.orchestration;

import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.order.entity.OrderStatus;
import com.nghiatr.ticket_booking.order.repository.OrderRepository;
import com.nghiatr.ticket_booking.payment.dto.PaymentCallbackResult;
import com.nghiatr.ticket_booking.payment.dto.PaymentReturnResult;
import com.nghiatr.ticket_booking.payment.entity.Payment;
import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import com.nghiatr.ticket_booking.payment.service.PaymentService;
import com.nghiatr.ticket_booking.payment.strategy.PaymentStrategy;
import com.nghiatr.ticket_booking.payment.strategy.PaymentStrategyFactory;
import com.nghiatr.ticket_booking.seat.entity.Seat;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.seat.repository.SeatRepository;
import com.nghiatr.ticket_booking.seat.service.SeatWebSocketPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;

    private final SeatWebSocketPublisher seatWebSocketPublisher;

    /**
     * Điều phối xử lý IPN/Webhook từ cổng thanh toán, cập nhật trạng thái đơn hàng và ghế ngồi khi thanh toán thành công.
     *
     * @param method phương thức thanh toán tương ứng
     * @param ipnParams tập các tham số nhận được từ webhook của cổng thanh toán
     * @return đối tượng PaymentCallbackResult chứa kết quả xác thực và thông tin giao dịch
     */
    @Transactional
    public PaymentCallbackResult processIpnPayment(PaymentMethod method, Map<String, String> ipnParams) {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(method);
        PaymentCallbackResult result = strategy.handleIpnCallback(ipnParams);

        if (result.success()) {
            Payment payment = paymentService.markSuccess(result.transactionCode(), result.gatewayTransactionId());
            Order order = payment.getOrder();

            if (order.getStatus() != OrderStatus.PAID) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                // Chuyển toàn bộ ghế từ PENDING sang BOOKED
                seatRepository.updateSeatStatusByOrder(SeatStatus.BOOKED, order);
                log.info("[PaymentFacade] Đã xác nhận thanh toán đơn hàng {} và chuyển ghế sang BOOKED", order.getOrderId());

                // Bắn event phát vé (Ticket Issuance) hoặc WebSocket broadcast trạng thái ghế
                List<Seat> seats = seatRepository.findByOrder(order);

                if(!seats.isEmpty()) {
                    seatWebSocketPublisher.publishSeatBooked(
                            seats.getFirst().getEventId().getEventId(),
                            seats.stream().map(Seat::getSeatId).toList()
                    );
                }
            }
        } else {
            paymentService.markFailed(result.transactionCode(), result.gatewayTransactionId());
            log.warn("[PaymentFacade] Thanh toán thất bại cho giao dịch {}", result.transactionCode());
        }

        return result;
    }

    /**
     * Phân tích các tham số từ URL trả về sau khi người dùng thực hiện thanh toán để chuẩn bị hiển thị kết quả.
     *
     * @param method phương thức thanh toán
     * @param returnParams tập các tham số trên query string khi trình duyệt chuyển hướng về
     * @return đối tượng PaymentReturnResult chứa trạng thái và mã giao dịch hiển thị trên giao diện
     */
    public PaymentReturnResult processReturnUrl(PaymentMethod method, Map<String, String> returnParams) {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(method);
        return strategy.parseReturnParams(returnParams);
    }
}
