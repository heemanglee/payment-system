package com.example.assignment.point.user.controller;

import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.user.dto.response.SignUpResponse;
import com.example.assignment.point.user.application.UserRegistrationFacade;
import com.example.assignment.point.user.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRegistrationFacade userRegistrationFacade;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest createUserRequest) {
        SignUpResponse response = userRegistrationFacade.registerWithLock(createUserRequest);
        return ResponseEntity.ok(response);
    }
}
