package com.nghiatr.ticket_booking.shared.utils;

import com.nghiatr.ticket_booking.shared.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ResponseUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Ghi đối tượng phản hồi lỗi ErrorResponse trực tiếp vào luồng HttpServletResponse dưới dạng JSON.
     *
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP gửi đi
     * @param status mã trạng thái HTTP số nguyên (ví dụ 400, 401, 500)
     * @param error tên trạng thái lỗi HTTP
     * @param errorCode mã lỗi nghiệp vụ của hệ thống
     * @param message thông điệp chi tiết mô tả lỗi
     * @throws IOException nếu xảy ra lỗi ghi luồng dữ liệu
     */
    public static void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String error,
            String errorCode,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = new ErrorResponse(
                status,
                error,
                errorCode,
                message,
                request.getServletPath(),
                Instant.now()
        );

        OBJECT_MAPPER.writeValue(response.getOutputStream(), errorResponse);
    }
}
