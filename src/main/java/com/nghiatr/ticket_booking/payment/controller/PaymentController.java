package com.nghiatr.ticket_booking.payment.controller;

import com.nghiatr.ticket_booking.orchestration.PaymentFacade;
import com.nghiatr.ticket_booking.payment.dto.CreatePaymentRequest;
import com.nghiatr.ticket_booking.payment.dto.PaymentResponse;
import com.nghiatr.ticket_booking.payment.dto.PaymentReturnResult;
import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import com.nghiatr.ticket_booking.payment.service.PaymentService;
import com.nghiatr.ticket_booking.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentFacade paymentFacade;

    /**
     * Khởi tạo URL - phiên thanh toán cho đơn hàng.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        PaymentResponse response = paymentService.initPayment(request, clientIp);
        return ResponseEntity.ok(ApiResponse.ok(response, "Khởi tạo phiên thanh toán thành công"));
    }

    /**
     * Nhận kết quả redirect từ trình duyệt sau khi người dùng hoàn tất thanh toán ở cổng thứ 3.
     * Đây là endpoint nhận kết quả trả về cho browser.
     * Endpoint này CHỈ đọc và hiển thị, không cập nhật trạng thái đơn hàng.
     */
    @GetMapping("/{method}/return")
    public ResponseEntity<ApiResponse<PaymentReturnResult>> handleBrowserReturn(
            @PathVariable("method") PaymentMethod method,
            @RequestParam Map<String, String> allParams
    ) {
        PaymentReturnResult result = paymentFacade.processReturnUrl(method, allParams);
        return ResponseEntity.ok(ApiResponse.ok(result, "Nhận kết quả giao dịch từ cổng thanh toán"));
    }
}
