package com.nghiatr.ticket_booking.user.model;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/* Hold the current user information perform the specific request.*/
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    @NullMarked
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                )
        );
    }

    @Override
    public boolean isEnabled() {
        return !this.user.isDeleted();
    }

    public UUID getUserId() {
        return this.user.getId();
    }

    public User getUser() {
        return this.user;
    }
}
