package com.nghiatr.ticket_booking.event.exception;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import org.springframework.http.HttpStatus;

public enum EventErrorCode implements ErrorCode {
    EVENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "EVENT_001",
            "Sự kiện không tồn tại"
    ),

    EVENT_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "EVENT_002",
            "Bạn không có quyền truy cập sự kiện"
    ),

    INVALID_EVENT_TIME(
            HttpStatus.BAD_REQUEST,
            "EVENT_003",
            "Thời gian sự kiện không hợp lệ"
    ),

    INVALID_TICKET_CLASS(
            HttpStatus.BAD_REQUEST,
            "EVENT_004",
            "Thông tin hạng vé không hợp lệ"
    ),

    VENUE_CHANGE_NOT_ALLOWED(
            HttpStatus.BAD_REQUEST,
            "EVENT_005",
            "Không được thay đổi địa điểm sau khi đã tạo layout"
    ),

    EMPTY_SEAT_LAYOUT(
            HttpStatus.BAD_REQUEST,
            "EMPTY_SEAT_LAYOUT",
            "Dữ liệu sơ đồ ghế (seat layout) không được để trống"
    ),

    VENUE_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "VENUE_NOT_FOUND",
            "Không tìm thấy địa điểm tổ chức"
    ),

    ORGANIZER_NOT_FOUND(
            HttpStatus.FORBIDDEN,
            "ORGANIZER_FORBIDDEN",
            "Bạn không có quyền hạn"
    ),

    EVENT_IMG_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "EVENT_IMG_NOT_FOUND",
            "Không tìm thấy ảnh bìa sự kiện. Vui lòng kiểm tra lại upload."
    ),

    LAYOUT_ALREADY_CREATED(
            HttpStatus.BAD_REQUEST,
            "LAYOUT_ALREADY_CREATED",
            "Layout đã được tạo. Liên hệ Admin để reset nếu cần thay đổi."
    ),

    VENUE_CANNOT_BE_CHANGED(
            HttpStatus.BAD_REQUEST,
            "VENUE_CANNOT_BE_CHANGED",
            "Không thể đổi địa điểm sau khi đã tạo sơ đồ ghế."
    ),

    EVENT_LOCKED(
            HttpStatus.FORBIDDEN,
            "EVENT_LOCKED",
            "Sự kiện đang ở trạng thái hiện tại, không thể chỉnh sửa."
    ),

    QUOTA_CANNOT_DECREASE(
            HttpStatus.BAD_REQUEST,
            "QUOTA_CANNOT_DECREASE",
            "Quota chỉ có thể tăng sau khi sự kiện được duyệt."
    );


    private final HttpStatus status;
    private final String code;
    private final String message;

    EventErrorCode(
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
