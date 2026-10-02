package com.nghiatr.ticket_booking.shared.storage;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import org.springframework.http.HttpStatus;

public enum FileErrorCode implements ErrorCode {
    INVALID_FILE(
            HttpStatus.BAD_REQUEST,
            "FILE_001",
            "File không hợp lệ hoặc rỗng."
    ),
    UNSUPPORTED_FILE_TYPE(
            HttpStatus.BAD_REQUEST,
            "FILE_002",
            "Chỉ chấp nhận các định dạng file ảnh: JPEG, PNG, WEBP."
    ),
    FILE_SIZE_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "FILE_003",
            "Kích thước file ảnh vượt quá dung lượng tối đa cho phép."
    ),
    FILE_UPLOAD_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FILE_004",
            "Upload file thất bại. Vui lòng thử lại sau."
    ),
    FILE_DELETE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FILE_005",
            "Xóa file thất bại."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    FileErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
