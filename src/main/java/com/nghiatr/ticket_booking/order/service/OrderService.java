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
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.user.model.Customer;
import com.nghiatr.ticket_booking.user.repository.CustomerRepository;
import io.micrometer.core.instrument.MeterRegistry;
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

    // 1. Find all order by userId;
    public OrderResponse findAll(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        return OrderResponse.from(
                orderRepository.findAllByCustomer(customer)
        );
    }

    // 2. Get Detail of an order;
    public OrderItemResponse findOne(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        return OrderItemResponse.from(order);
    }

    // 3. Create Order + Holding seat (PENDING) -> Create Payment.
    // Refactor later, using some technique.
    @Transactional
    public CreateOrderResponse create(UUID customerId, List<UUID> seatIds) {

        if(seatIds == null || seatIds.isEmpty() || seatIds.size() > 5) {
            throw new AppException(OrderErrorCode.SEAT_LIMIT_EXCEEDED);
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        // 1. Get Seats Information
        List<Seat> seats = seatRepository.findBySeatIdIn(seatIds);

        // 2. All seats must be AVAILABLE.
        if(seats.size() != seatIds.size()) {
            throw new AppException(OrderErrorCode.UNEXISTED_SEAT);
        }

        // 4. Compute the total amount:
        double totalAmount = seats.stream()
                .reduce(0.0, (total, ele) -> total + ele.getTicketClassId().getPrice(), Double::sum);

        // 5. Calculate Hold Expiration
        LocalDateTime holdExpiredAt = LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES);

        // 6. create Order:
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

        orderRepository.save(
                order
        );

        // 3. Handle Race condition at here. Will changing for later.
        /*
        Các giải pháp:
            1. Persimisstic Locking: Đảm bảo tuyệt đối và dễ Implelement: SQL FOR UPDATE.
            -> Throughput thấp, block các request, dễ gây deadlock nếu không lock
                theo thứ tự cố định.
            2. Optimisstic Locking - With version: (Sử dụng annotation @Version trong entity)
                Không blocking, throghput cao khi có ít conflict.
            -> Cần retry logic, không nên khi có nhiều conflict đồng thời.
            3. Atomic Update với điều kiện (Compare-and-Swap)
                Đơn giản và hiệu quả, không cần Lock ở tầng JPA, giữ được tính atomic ở tầng DB
            -> Không biết là ghế nào bị lấy mất để trả về tầng Service.

        * */
        List<Seat> unavailableSeats = seats.stream()
                .filter(seat -> seat.getStatus() != SeatStatus.AVAILABLE)
                .toList();

        if(!unavailableSeats.isEmpty()) {
            String seatNames = unavailableSeats.stream()
                    .map(Seat::getName)
                    .collect(Collectors.joining(","));

            throw new AppException((OrderErrorCode.UNAVAILABLE_SEATS));
        }

        // 7. Update seats - Reserve seats
        seatRepository.updateSeatStatus(SeatStatus.PENDING, holdExpiredAt, order, seatIds);
        List<Seat> newSeats = seatRepository.findBySeatIdIn(seatIds);

        // 8. Create Payments
        // Implements later.

        // 9. Return DTO:
        return CreateOrderResponse.from(order, newSeats, holdExpiredAt);
    }

}
