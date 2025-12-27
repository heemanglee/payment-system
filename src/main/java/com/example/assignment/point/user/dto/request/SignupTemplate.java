package com.example.assignment.point.user.dto.request;

public record SignupTemplate(
    String email,
    String password,
    String username,
    String verificationCode
) {
}
