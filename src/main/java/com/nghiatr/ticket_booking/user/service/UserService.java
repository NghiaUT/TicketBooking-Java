package com.nghiatr.ticket_booking.user.service;

import com.nghiatr.ticket_booking.user.model.*;
import com.nghiatr.ticket_booking.user.repository.AdminRepository;
import com.nghiatr.ticket_booking.user.repository.CustomerRepository;
import com.nghiatr.ticket_booking.user.repository.OrganizerRepository;
import com.nghiatr.ticket_booking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final OrganizerRepository organizerRepository;
    private final AdminRepository adminRepository;

    public User createUser(
            String name,
            String email,
            String password,
            PasswordEncoder passwordEncoder,
            UserRole role
    ) {
        User user =  User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        return userRepository.save(user);
    }

    public Customer createCustomer(
            User user
    ) {
        Customer customer = Customer.builder()
                .user(user)
                .build();

        return customerRepository.save(customer);
    }

    public Organizer createOrganizer(
            User user
    ) {
        Organizer organizer = Organizer.builder()
                .user(user)
                .build();

        return  organizerRepository.save(organizer);
    }

    public Admin createAdmin(
            User user
    ) {
        Admin admin = Admin.builder()
                .user(user)
                .build();

        return  adminRepository.save(admin);
    }
}
