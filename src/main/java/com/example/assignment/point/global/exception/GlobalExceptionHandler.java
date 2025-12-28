package com.example.assignment.point.global.exception;

import com.example.assignment.point.user.exception.DuplicateEmailException;
import com.example.assignment.point.user.exception.InvalidVerificationCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException ex) {
        return ResponseEntity.status(ex.getStatus())
            .body(ErrorResponse.of(ex.getStatus().value(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCodeException(InvalidVerificationCodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(ex.getStatus().value(), ex.getMessage()));
    }
}
