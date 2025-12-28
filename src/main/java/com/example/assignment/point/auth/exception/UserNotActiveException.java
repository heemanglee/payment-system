package com.example.assignment.point.auth.exception;

import com.example.assignment.point.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class UserNotActiveException extends CustomException {

    public UserNotActiveException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
