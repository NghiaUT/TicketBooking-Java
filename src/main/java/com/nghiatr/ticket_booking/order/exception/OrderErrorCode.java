package com.nghiatr.ticket_booking.order.exception;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "ORDER_NOT_FOUND",
            "Order này không tồn tại."
    ),

    SEAT_LIMIT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "SEAT_LIMIT_EXCEEDED",
            "Chỉ được chọn từ 1 đến 5 ghế."
    ),

    UNEXISTED_SEAT(
            HttpStatus.BAD_REQUEST,
            "UNEXISTED_SEAT",
            "Một hoặc nhiều ghế không tồn tại."
    ),

    UNAVAILABLE_SEATS(
            HttpStatus.BAD_REQUEST,
            "UNAVAILABLE_SEATS",
            "Các ghế hiện tại không khả dụng."
    )
    ;


    private final HttpStatus status;
    private final String code;
    private final String message;

    OrderErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
