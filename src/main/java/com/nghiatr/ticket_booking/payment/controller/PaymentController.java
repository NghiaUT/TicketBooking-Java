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
     * Khởi tạo phiên thanh toán cho đơn hàng và sinh URL thanh toán tương ứng.
     *
     * @param request thông tin yêu cầu thanh toán bao gồm ID đơn hàng, phương thức và URL quay về
     * @param servletRequest yêu cầu HTTP từ phía client để trích xuất địa chỉ IP
     * @return phản hồi HTTP chứa ApiResponse với PaymentResponse gồm thông tin thanh toán và URL
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
     * Phân tích và hiển thị kết quả redirect từ trình duyệt sau khi người dùng hoàn tất thanh toán ở cổng thứ 3.
     *
     * @param method phương thức thanh toán đã sử dụng
     * @param allParams tập các tham số query do cổng thanh toán gửi kèm trên URL
     * @return phản hồi HTTP chứa ApiResponse với PaymentReturnResult
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
