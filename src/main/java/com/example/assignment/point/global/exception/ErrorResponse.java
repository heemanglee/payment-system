package com.example.assignment.point.global.exception;

import java.util.List;

public record ErrorResponse(
    int status,
    String message,
    List<FieldError> errors // validation error message
) {

    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, List.of());
    }

    public static ErrorResponse of(int status, String message, List<FieldError> errors) {
        return new ErrorResponse(status, message, errors);
    }
}
