package com.nghiatr.ticket_booking.shared.dto;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    public HttpStatus getStatus();
    public String getCode();
    public String getMessage();
}