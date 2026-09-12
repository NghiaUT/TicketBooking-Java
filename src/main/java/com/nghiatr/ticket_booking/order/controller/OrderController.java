package com.nghiatr.ticket_booking.order.controller;

import com.nghiatr.ticket_booking.order.dto.CreateOrderRequest;
import com.nghiatr.ticket_booking.order.dto.CreateOrderResponse;
import com.nghiatr.ticket_booking.order.dto.OrderItemResponse;
import com.nghiatr.ticket_booking.order.dto.OrderResponse;
import com.nghiatr.ticket_booking.order.service.OrderService;
import com.nghiatr.ticket_booking.shared.dto.ApiResponse;
import com.nghiatr.ticket_booking.user.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<OrderResponse>> findAllOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        UUID customerId = userDetails.getUserId();

        return ResponseEntity.ok(
                ApiResponse.ok(
                        orderService.findAll(customerId),
                        "Lấy danh sách đơn hàng thành công."
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderItemResponse>> findOneOrderDetail(
            @PathVariable("id") UUID orderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        orderService.findOne(orderId),
                        "Lấy chi tiết đơn hàng thành công."
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Validated @RequestBody CreateOrderRequest orderRequest
            ) {
        UUID customerId = userDetails.getUserId();
        // Add socket later.
        return ResponseEntity.ok(
                ApiResponse.ok(
                        orderService.create(customerId, orderRequest.seatIds()),
                        "Tạo đơn hàng thành công."
                )
        );
    }
}
