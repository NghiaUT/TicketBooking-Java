package com.nghiatr.ticket_booking.shared.dto;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    /**
     * Lấy mã trạng thái HTTP tương ứng với lỗi.
     *
     * @return đối tượng HttpStatus
     */
    public HttpStatus getStatus();

    /**
     * Lấy mã chuỗi định danh đặc trưng của lỗi.
     *
     * @return mã lỗi dạng chuỗi
     */
    public String getCode();

    /**
     * Lấy thông điệp mô tả chi tiết lỗi dành cho người dùng.
     *
     * @return nội dung thông báo lỗi
     */
    public String getMessage();
}