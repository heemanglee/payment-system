package com.example.assignment.point.user.exception;

import com.example.assignment.point.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends CustomException {

    public DuplicateEmailException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
