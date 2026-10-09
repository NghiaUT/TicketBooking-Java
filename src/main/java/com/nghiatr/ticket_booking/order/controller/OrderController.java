package com.nghiatr.ticket_booking.order.controller;

import com.nghiatr.ticket_booking.order.dto.CreateOrderRequest;
import com.nghiatr.ticket_booking.order.dto.CreateOrderResponse;
import com.nghiatr.ticket_booking.order.dto.OrderItemResponse;
import com.nghiatr.ticket_booking.order.dto.OrderResponse;
import com.nghiatr.ticket_booking.order.service.OrderService;
import com.nghiatr.ticket_booking.shared.dto.ApiResponse;
import com.nghiatr.ticket_booking.user.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Orders", description = "Quản lý đặt vé và đơn hàng của khách hàng")
@SecurityRequirement(name = "BearerAuth")
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
    @Operation(summary = "Lấy danh sách đơn hàng", description = "Lấy danh sách toàn bộ đơn hàng của khách hàng đang đăng nhập")
    @GetMapping
    public ResponseEntity<ApiResponse<OrderResponse>> findAllOrders(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
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
    @Operation(summary = "Lấy chi tiết đơn hàng", description = "Lấy thông tin chi tiết của một đơn hàng theo mã định danh (UUID)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderItemResponse>> findOneOrderDetail(
            @Parameter(description = "ID duy nhất của đơn hàng (UUID)", required = true) @PathVariable("id") UUID orderId
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
    @Operation(summary = "Tạo mới đơn hàng", description = "Tạo mới đơn hàng và tạm giữ chỗ các ghế đã chọn trong khoảng thời gian quy định")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
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
