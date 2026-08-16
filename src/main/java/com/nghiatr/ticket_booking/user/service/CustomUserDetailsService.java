package com.nghiatr.ticket_booking.user.service;

import com.nghiatr.ticket_booking.user.model.CustomUserDetails;
import com.nghiatr.ticket_booking.user.model.User;
import com.nghiatr.ticket_booking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        System.out.println("Email of user: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user"));

        return new CustomUserDetails(user);
    }
}
