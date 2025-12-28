package com.example.assignment.point.auth.application;

import com.example.assignment.point.auth.domain.service.AuthService;
import com.example.assignment.point.auth.dto.request.LoginRequest;
import com.example.assignment.point.auth.dto.request.LogoutRequest;
import com.example.assignment.point.auth.dto.request.RefreshTokenRequest;
import com.example.assignment.point.auth.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;

    public TokenResponse login(LoginRequest request) {
        return authService.login(request);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    public void logout(Long userId, LogoutRequest request) {
        authService.logout(userId, request.refreshToken());
    }
}
