package com.nghiatr.ticket_booking.venue.exception;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import org.springframework.http.HttpStatus;

public enum VenueErrorCode implements ErrorCode {
    VENUE_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "VENUE_NOT_FOUND",
            "Không tìm thấy địa điểm tổ chức tương ứng."
    );


    private final HttpStatus status;
    private final String code;
    private final String message;

    VenueErrorCode(
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
