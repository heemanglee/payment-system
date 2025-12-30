package com.example.assignment.point.user.exception;

import com.example.assignment.point.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class RegistrationFailedException extends CustomException {

    public RegistrationFailedException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}
