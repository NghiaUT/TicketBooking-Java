package com.nghiatr.ticket_booking.shared.handler;

import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.shared.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Bắt lỗi Validation từ các DTO
    /**
     * Xử lý ngoại lệ vi phạm ràng buộc dữ liệu đầu vào (Validation) từ các DTO.
     *
     * @param ex ngoại lệ MethodArgumentNotValidException
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP gửi đi
     * @throws IOException nếu xảy ra lỗi ghi dữ liệu phản hồi
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        Map<String, String> errors = new HashMap<>();

        // Lặp qua tất cả các field bị lỗi và lấy ra "message" bạn đã định nghĩa
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String messages = String.join(", ", errors.values());

        ResponseUtil.writeErrorResponse(
                request,
                response,
                HttpStatus.BAD_REQUEST.value(),
                String.valueOf(HttpStatus.BAD_REQUEST),
                "VALIDATION",
                messages
        );
    }

    /**
     * Xử lý ngoại lệ nghiệp vụ nội bộ AppException của hệ thống.
     *
     * @param ex ngoại lệ AppException
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP gửi đi
     * @throws IOException nếu xảy ra lỗi ghi dữ liệu phản hồi
     */
    @ExceptionHandler(AppException.class)
    public void handleAppException(
            AppException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        ErrorCode errorCode = ex.getErrorCode();
        String message = ex.getMessage();

        ResponseUtil.writeErrorResponse(
                request,
                response,
                errorCode.getStatus().value(),
                errorCode.getStatus().toString(),
                errorCode.getCode(),
                message
        );
    }

    /**
     * Xử lý các lỗi ngoại lệ chung chưa được định nghĩa trước.
     *
     * @param ex ngoại lệ Exception
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP gửi đi
     * @throws IOException nếu xảy ra lỗi ghi dữ liệu phản hồi
     */
    @ExceptionHandler(Exception.class)
    public void handleGeneralException(
            Exception ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        ResponseUtil.writeErrorResponse(
                request,
                response,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                "INTERNAL_SERVER_ERROR_001",
                ex.getMessage()
        );
    }

    /**
     * Xử lý lỗi vi phạm ràng buộc toàn vẹn dữ liệu trong cơ sở dữ liệu.
     *
     * @param ex ngoại lệ DataIntegrityViolationException
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP gửi đi
     * @throws IOException nếu xảy ra lỗi ghi dữ liệu phản hồi
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public void handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        ResponseUtil.writeErrorResponse(
                request,
                response,
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.toString(),
                "DATA_INTEGRITY_VIOLATION",
                "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc của hệ thống."
        );
    }

    /**
     * Xử lý lỗi khi dung lượng tệp tin tải lên vượt quá giới hạn cấu hình của hệ thống.
     *
     * @param ex ngoại lệ MaxUploadSizeExceededException
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP gửi đi
     * @throws IOException nếu xảy ra lỗi ghi dữ liệu phản hồi
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public void handleMaxUploadSizeExceeded(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        ResponseUtil.writeErrorResponse(
                request,
                response,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.toString(),
                "FILE_003",
                "Kích thước file vượt quá dung lượng tối đa cho phép của hệ thống."
        );
    }
}
