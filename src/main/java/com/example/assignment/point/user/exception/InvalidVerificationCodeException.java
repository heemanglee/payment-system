package com.example.assignment.point.user.exception;

import com.example.assignment.point.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationCodeException extends CustomException {

    public InvalidVerificationCodeException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
