package com.nghiatr.ticket_booking.shared.utils;

import com.nghiatr.ticket_booking.user.model.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class SecurityUtils {
    /**
     * Lấy thông tin CustomUserDetails của người dùng hiện tại từ SecurityContextHolder.
     *
     * @return đối tượng CustomUserDetails của người dùng đang đăng nhập
     */
    public CustomUserDetails getCurrentUser() {
        return (CustomUserDetails)
                Objects.requireNonNull(SecurityContextHolder.getContext()
                                .getAuthentication())
                        .getPrincipal();
    }

    /**
     * Lấy mã định danh duy nhất (userId) của người dùng hiện tại đang đăng nhập.
     *
     * @return UUID của người dùng hiện tại
     */
    public UUID getCurrentUserId() {
        return getCurrentUser().getUser().getId();
    }
}
