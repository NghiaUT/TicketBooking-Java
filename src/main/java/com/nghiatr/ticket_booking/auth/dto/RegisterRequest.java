package com.nghiatr.ticket_booking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username cannot be empty!")
    @Size(min = 4, max = 50, message = "Username muse be from 4 - 50 characters")
    private String name;

    @NotBlank(message = "Email cannot be empty!")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String fullName;
}
