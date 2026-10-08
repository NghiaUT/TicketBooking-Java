package com.nghiatr.ticket_booking.shared.handler;

import com.nghiatr.ticket_booking.shared.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Xử lý trường hợp người dùng bị từ chối truy cập do thiếu quyền hạn (HTTP 403 Forbidden).
     *
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP gửi đi
     * @param accessDeniedException ngoại lệ AccessDeniedException xảy ra
     * @throws IOException nếu xảy ra lỗi ghi dữ liệu phản hồi
     */
    @Override
    public void handle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ResponseUtil.writeErrorResponse(
                request,
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                "FORBIDDEN",
                "Bạn không có quyền truy cập tài nguyên này!"
        );
    }
}
