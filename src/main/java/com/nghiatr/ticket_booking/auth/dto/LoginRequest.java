package com.nghiatr.ticket_booking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username or email cannot be empty")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Password can not be empty")
    private String password;
}
