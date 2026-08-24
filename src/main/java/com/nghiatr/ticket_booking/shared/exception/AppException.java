package com.nghiatr.ticket_booking.shared.exception;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;

public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}