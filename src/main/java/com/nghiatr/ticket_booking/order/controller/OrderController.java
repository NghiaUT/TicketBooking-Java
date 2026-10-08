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

    /**
     * Lấy danh sách toàn bộ đơn hàng của khách hàng hiện tại.
     *
     * @param userDetails thông tin người dùng khách hàng đang đăng nhập
     * @return phản hồi HTTP chứa danh sách các đơn hàng
     */
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

    /**
     * Lấy thông tin chi tiết của một đơn hàng theo mã đơn hàng.
     *
     * @param orderId định danh duy nhất của đơn hàng
     * @return phản hồi HTTP chứa thông tin chi tiết đơn hàng
     */
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

    /**
     * Tạo mới đơn hàng và giữ chỗ các ghế được chọn.
     *
     * @param userDetails thông tin người dùng khách hàng đang đăng nhập
     * @param orderRequest dữ liệu yêu cầu tạo đơn hàng chứa danh sách mã ghế
     * @return phản hồi HTTP chứa thông tin đơn hàng vừa tạo và danh sách ghế giữ chỗ
     */
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
