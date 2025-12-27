package com.example.assignment.point.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    String password,

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 8, message = "Username must be between 3 and 8 characters")
    String username,

    @Size(min = 6, max = 6)
    String verificationCode
) {
}

