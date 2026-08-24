package com.nghiatr.ticket_booking.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<T>(true, data, null); }

    public static <T> ApiResponse<T> ok(T data, String message) { return new ApiResponse<T>(true, data, message); }

    public boolean isSuccess()          { return success; }
    public T getData()                  { return data; }
    public String getMessage()          { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
