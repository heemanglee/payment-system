package com.example.assignment.point.user.exception;

import com.example.assignment.point.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class RegistrationConcurrencyException extends CustomException {

    public RegistrationConcurrencyException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
