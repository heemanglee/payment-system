package com.example.assignment.point.user.controller;

import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.user.dto.response.CreateUserResponse;
import com.example.assignment.point.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequest createUserRequest) {
        String verificationCode = userService.register(createUserRequest);
        return ResponseEntity.ok(verificationCode);
    }

    @PostMapping("/verify")
    public ResponseEntity<CreateUserResponse> verify(@Valid @RequestBody SignUpRequest request) {
        CreateUserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
