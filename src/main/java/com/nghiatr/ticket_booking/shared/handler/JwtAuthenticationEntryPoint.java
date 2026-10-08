package com.nghiatr.ticket_booking.shared.handler;

import com.nghiatr.ticket_booking.shared.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Xử lý trường hợp người dùng chưa xác thực hoặc token không hợp lệ (HTTP 401 Unauthorized).
     *
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP gửi đi
     * @param authException ngoại lệ AuthenticationException xảy ra
     * @throws IOException nếu xảy ra lỗi ghi dữ liệu phản hồi
     */
    @Override
    public void commence(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull AuthenticationException authException) throws IOException {
        ResponseUtil.writeErrorResponse(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "UNAUTHORIZED",
                "Yêu cầu xác thực. Vui lòng cung cấp access token hợp lệ."
        );
    }
}
