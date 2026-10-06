package com.nghiatr.ticket_booking.auth.exception;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    BAD_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "BAD_CREDENTIALS",
            "Email hoặc mật khẩu không chính xác."
    ),
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "Tài khoản không tồn tại."
    ),
    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "EMAIL_ALREADY_EXISTS",
            "Email đã được sử dụng."
    ),
    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "INVALID_REFRESH_TOKEN",
            "Refresh token không hợp lệ hoặc đã hết hạn."
    ),
    ADMIN_REGISTER_FORBIDDEN(
            HttpStatus.UNAUTHORIZED,
            "ADMIN_REGISTER_FORBIDDEN",
            "Không thể đăng ký với tư cách Admin."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}