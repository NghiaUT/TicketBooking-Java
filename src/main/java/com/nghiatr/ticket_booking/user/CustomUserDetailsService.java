package com.nghiatr.ticket_booking.user;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    public CustomUserDetails loadUserByUsername(String id) {
        UUID userId = UUID.fromString(id);

        User user = userRepository.findById(userId)
                .orElseThrow();

        return new CustomUserDetails(user);
    }
}
