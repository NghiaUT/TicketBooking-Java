package com.nghiatr.ticket_booking.shared.utils;

import com.nghiatr.ticket_booking.user.model.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class SecurityUtils {
    public CustomUserDetails getCurrentUser() {
        return (CustomUserDetails)
                Objects.requireNonNull(SecurityContextHolder.getContext()
                                .getAuthentication())
                        .getPrincipal();
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getUser().getId();
    }
}
