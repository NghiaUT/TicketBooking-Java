package com.nghiatr.ticket_booking.ticketClass.exception;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import org.springframework.http.HttpStatus;

public enum TicketClassErrorCode implements ErrorCode {
    TICKET_CLASS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "TICKET_CLASS_001",
            "Không tìm thấy hạng vé tương ứng"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    TicketClassErrorCode(
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
