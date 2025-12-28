package com.example.assignment.point.auth.controller;

import com.example.assignment.point.auth.application.AuthFacade;
import com.example.assignment.point.auth.dto.request.LoginRequest;
import com.example.assignment.point.auth.dto.request.LogoutRequest;
import com.example.assignment.point.auth.dto.request.RefreshTokenRequest;
import com.example.assignment.point.auth.dto.response.TokenResponse;
import com.example.assignment.point.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authFacade.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authFacade.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody LogoutRequest request
    ) {
        authFacade.logout(userDetails.getUserId(), request);
        return ResponseEntity.ok(Map.of("message", "Successfully logged out"));
    }
}
