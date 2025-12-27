package com.example.assignment.point.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid input value"),

    // User
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "Duplicate email");

    private final HttpStatus status;
    private final String message;
}
