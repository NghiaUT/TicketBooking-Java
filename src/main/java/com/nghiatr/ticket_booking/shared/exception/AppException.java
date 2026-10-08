package com.nghiatr.ticket_booking.shared.exception;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;

public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Khởi tạo ngoại lệ ứng dụng từ định nghĩa mã lỗi nghiệp vụ ErrorCode.
     *
     * @param errorCode đối tượng mã lỗi định nghĩa trước
     */
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Khởi tạo ngoại lệ ứng dụng từ mã lỗi ErrorCode kèm thông điệp chi tiết tùy biến.
     *
     * @param errorCode đối tượng mã lỗi định nghĩa trước
     * @param message thông điệp mô tả chi tiết lỗi tùy chỉnh
     */
    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Lấy mã lỗi nghiệp vụ gắn liền với ngoại lệ.
     *
     * @return đối tượng ErrorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}