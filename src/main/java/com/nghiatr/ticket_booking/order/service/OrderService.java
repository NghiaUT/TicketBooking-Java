package com.nghiatr.ticket_booking.order.service;

import com.nghiatr.ticket_booking.order.dto.CreateOrderResponse;
import com.nghiatr.ticket_booking.order.dto.OrderItemResponse;
import com.nghiatr.ticket_booking.order.dto.OrderResponse;
import com.nghiatr.ticket_booking.order.entity.Order;
import com.nghiatr.ticket_booking.order.entity.OrderStatus;
import com.nghiatr.ticket_booking.order.exception.OrderErrorCode;
import com.nghiatr.ticket_booking.order.repository.OrderRepository;
import com.nghiatr.ticket_booking.seat.entity.Seat;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.seat.repository.SeatRepository;
import com.nghiatr.ticket_booking.seat.service.SeatWebSocketPublisher;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.user.model.Customer;
import com.nghiatr.ticket_booking.user.repository.CustomerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final int HOLD_DURATION_MINUTES = 5;

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final SeatRepository seatRepository;

    private final MeterRegistry meterRegistry;
    private final SeatWebSocketPublisher seatWebSocketPublisher;

    // For Counting
    private Counter orderCreated;
    private Counter unavailableSeat;

    @PostConstruct
    private void initMetrics() {
        this.orderCreated = Counter.builder("ticket_booking_orders_created")
                .description("Number of successfully created orders")
                .register(meterRegistry);

        this.unavailableSeat = Counter.builder("ticket_booking_unavailable_seats")
                .description("Number of orders rejected because seats are unavailable")
                .register(meterRegistry);
    }

    // 1. Find all order by userId;
    /**
     * Lấy danh sách toàn bộ đơn hàng của khách hàng theo mã định danh khách hàng.
     *
     * @param customerId định danh duy nhất của khách hàng
     * @return đối tượng OrderResponse chứa danh sách đơn hàng
     * @throws AppException nếu không tìm thấy thông tin khách hàng
     */
    public OrderResponse findAll(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        return OrderResponse.from(
                orderRepository.findAllByCustomer(customer)
        );
    }

    // 2. Get Detail of an order;
    /**
     * Lấy thông tin chi tiết của một đơn hàng theo mã đơn hàng.
     *
     * @param orderId định danh duy nhất của đơn hàng
     * @return đối tượng OrderItemResponse chứa thông tin đơn hàng
     * @throws AppException nếu đơn hàng không tồn tại
     */
    public OrderItemResponse findOne(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        return OrderItemResponse.from(order);
    }

    // 3. Create Order + Holding seat (PENDING) -> Create Payment.
    /**
     * Tạo mới đơn hàng và thực hiện giữ chỗ (lock ghế) nguyên tử trong khoảng thời gian quy định.
     *
     * @param customerId định danh duy nhất của khách hàng
     * @param seatIds danh sách định danh của các ghế muốn đặt
     * @return đối tượng CreateOrderResponse chứa thông tin đơn hàng và ghế đã giữ chỗ thành công
     * @throws AppException nếu vượt quá giới hạn ghế (tối đa 5 ghế), ghế không tồn tại hoặc ghế đã có người giữ
     */
    @Transactional
    public CreateOrderResponse create(UUID customerId, List<UUID> seatIds) {
        if(seatIds == null || seatIds.isEmpty() || seatIds.size() > 5) {
            throw new AppException(OrderErrorCode.SEAT_LIMIT_EXCEEDED);
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        // 1. Get Seats Information
        List<Seat> seats = seatRepository.findBySeatIdIn(seatIds);

        // 2. Validate all requested seats exist in the DB
        if(seats.size() != seatIds.size()) {
            throw new AppException(OrderErrorCode.UNEXISTED_SEAT);
        }

        // 3. Fast-fail: kiểm tra tình trạng ghế TRƯỚC khi tạo Order.
        //    Bước này KHÔNG có lock nên không chống được race condition,
        //    nhưng giúp fail nhanh với các ghế rõ ràng đã không khả dụng
        //    mà không cần tạo thêm một Order entity thừa.
        List<Seat> notAvailableSeats = seats.stream()
                .filter(seat -> seat.getStatus() != SeatStatus.AVAILABLE)
                .toList();
        if (!notAvailableSeats.isEmpty()) {
            String seatNames = notAvailableSeats.stream()
                    .map(Seat::getName)
                    .collect(Collectors.joining(", "));
            unavailableSeat.increment();
            throw new AppException(OrderErrorCode.UNAVAILABLE_SEATS,
                    "Các ghế " + seatNames + " đã không còn khả dụng.");
        }

        // 4. Compute the total amount
        double totalAmount = seats.stream()
                .reduce(0.0, (total, ele) -> total + ele.getTicketClassId().getPrice(), Double::sum);

        // 5. Calculate Hold Expiration
        LocalDateTime holdExpiredAt = LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES);

        // 6. Create Order — nằm trong cùng @Transactional, nếu bước 7 thất bại
        //    và throw exception, toàn bộ transaction sẽ rollback (kể cả Order này).
        Order order = Order.builder()
                .customer(customer)
                .customerEmail(customer.getUser().getEmail())
                .customerName(customer.getUser().getName())
                .customerPhone(customer.getUser().getPhoneNumber())
                .totalAmount(totalAmount)
                .numTicket(seats.size())
                .status(OrderStatus.PENDING)
                .expiredAt(holdExpiredAt)
                .build();

        orderRepository.save(order);

        // 7. Atomic CAS update — lớp bảo vệ thực sự chống race condition.
        //    DB chỉ update những ghế CÒN status = AVAILABLE tại thời điểm execute,
        //    đảm bảo tính atomic ngay cả khi nhiều request tranh nhau cùng lúc.
        Timer.Sample sample = Timer.start(meterRegistry);
        int updatedCount;
        try {
            updatedCount = orderRepository.updateSeatStatusIfAvailable(
                    SeatStatus.PENDING, order, holdExpiredAt, seatIds
            );
        } finally {
            sample.stop(
                    Timer.builder("ticket_booking_seat_reservation")
                            .description("Time spent reserving seats")
                            .publishPercentiles(0.5, 0.95, 0.99)
                            .register(meterRegistry)
            );
        }

        // 8. Nếu không lock đủ số ghế yêu cầu → có race condition xảy ra.
        //    Transaction này sẽ rollback toàn bộ (Order + seat update).
        if(updatedCount != seatIds.size()) {
            // Tìm đúng ghế bị người khác giữ: loại trừ ghế mình đã lock thành công
            // trong cùng transaction (để tránh báo lỗi sai tên ghế).
            List<Seat> takenSeats = seatRepository.findUnavailableSeatsExcludingOrder(
                    seatIds, SeatStatus.AVAILABLE, order.getOrderId()
            );
            String takenSeatNames = takenSeats.stream()
                    .map(Seat::getName)
                    .collect(Collectors.joining(", "));
            unavailableSeat.increment();
            throw new AppException(OrderErrorCode.UNAVAILABLE_SEATS,
                    "Các ghế " + takenSeatNames + " đã không còn khả dụng.");
        }

        List<Seat> newSeats = seatRepository.findBySeatIdIn(seatIds);
        // 9. Broadcast event lock ghế.
        seatWebSocketPublisher.publishSeatLocked(
                seats.getFirst().getEventId().getEventId(), seatIds, holdExpiredAt
        );

        // 10. Return DTO — tất cả ghế đã được reserve thành công
        orderCreated.increment();

        return CreateOrderResponse.from(order, newSeats, holdExpiredAt);
    }
}
