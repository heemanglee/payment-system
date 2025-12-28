package com.example.assignment.point.auth.exception;

import com.example.assignment.point.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends CustomException {

    public TokenExpiredException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}