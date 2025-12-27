package com.example.assignment.point.user.exception;

import com.example.assignment.point.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class VerificationCodeExpiredException extends CustomException {

    public VerificationCodeExpiredException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

}
