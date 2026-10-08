package com.nghiatr.ticket_booking.payment.exception;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PAYMENT_001",
            "Giao dịch thanh toán không tồn tại."
    ),
    PAYMENT_ALREADY_PROCESSED(
            HttpStatus.CONFLICT,
            "PAYMENT_002",
            "Giao dịch thanh toán đã được xử lý trước đó."
    ),
    UNSUPPORTED_PAYMENT_METHOD(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_003",
            "Phương thức thanh toán không được hỗ trợ."
    ),
    ORDER_ALREADY_PAID(
            HttpStatus.CONFLICT,
            "PAYMENT_004",
            "Đơn hàng này đã được thanh toán thành công."
    ),
    ORDER_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_005",
            "Đơn hàng đã hết thời gian giữ vé hoặc đã bị hủy."
    ),
    INVALID_PAYMENT_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_006",
            "Số tiền thanh toán không khớp với giá trị đơn hàng."
    ),
    INVALID_CHECKSUM_SIGNATURE(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_007",
            "Chữ ký xác thực dữ liệu từ cổng thanh toán không hợp lệ."
    ),
    GATEWAY_COMMUNICATION_ERROR(
            HttpStatus.BAD_GATEWAY,
            "PAYMENT_008",
            "Lỗi kết nối tới cổng thanh toán đối tác."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    PaymentErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
