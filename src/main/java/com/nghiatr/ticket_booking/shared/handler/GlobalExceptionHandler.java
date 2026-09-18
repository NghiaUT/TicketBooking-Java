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
                "Conflict",
                "DATA_INTEGRITY_VIOLATION",
                "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc của hệ thống."
        );
    }
}
