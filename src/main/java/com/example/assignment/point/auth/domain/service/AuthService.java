package com.example.assignment.point.auth.domain.service;

import com.example.assignment.point.auth.dto.request.LoginRequest;
import com.example.assignment.point.auth.dto.response.TokenResponse;
import com.example.assignment.point.auth.exception.InvalidCredentialsException;
import com.example.assignment.point.auth.exception.InvalidTokenException;
import com.example.assignment.point.auth.exception.TokenExpiredException;
import com.example.assignment.point.auth.exception.UserNotActiveException;
import com.example.assignment.point.global.config.JwtProperties;
import com.example.assignment.point.user.domain.entity.User;
import com.example.assignment.point.user.domain.entity.UserStatus;
import com.example.assignment.point.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        validatePassword(user, request.password());
        validateUserStatus(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        saveRefreshToken(user.getId(), jwtTokenProvider.getTokenIdFromToken(refreshToken), refreshToken);

        return TokenResponse.of(
            accessToken,
            refreshToken,
            jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            if (jwtTokenProvider.isTokenExpired(refreshToken)) {
                throw new TokenExpiredException("Refresh token has expired");
            }
            throw new InvalidTokenException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenTypeFromToken(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new InvalidTokenException("Invalid token type");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String tokenId = jwtTokenProvider.getTokenIdFromToken(refreshToken);

        if (!isRefreshTokenValid(userId, tokenId)) {
            throw new InvalidTokenException("Refresh token not found or already revoked");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidTokenException("User not found"));

        validateUserStatus(user);

        deleteRefreshToken(userId, tokenId);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        saveRefreshToken(user.getId(), jwtTokenProvider.getTokenIdFromToken(newRefreshToken), newRefreshToken);

        return TokenResponse.of(
            newAccessToken,
            newRefreshToken,
            jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }

    @Transactional
    public void logout(Long userId, String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return;
        }

        String tokenId = jwtTokenProvider.getTokenIdFromToken(refreshToken);
        deleteRefreshToken(userId, tokenId);
    }

    private void validatePassword(User user, String rawPassword) {
        String hashedPassword = user.getPassword();

        if (!passwordEncoder.matches(rawPassword, hashedPassword)) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    private void validateUserStatus(User user) {
        if (user.getUserStatus() == UserStatus.SUSPENDED) {
            throw new UserNotActiveException("User account is suspended");
        }
        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            throw new UserNotActiveException("User account has been withdrawn");
        }
    }

    private void saveRefreshToken(Long userId, String tokenId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId + ":" + tokenId;
        redisTemplate.opsForValue().set(
            key,
            refreshToken,
            jwtProperties.getRefreshTokenExpiration(),
            TimeUnit.MILLISECONDS
        );
    }

    private void deleteRefreshToken(Long userId, String tokenId) {
        String key = REFRESH_TOKEN_PREFIX + userId + ":" + tokenId;
        redisTemplate.delete(key);
    }

    private boolean isRefreshTokenValid(Long userId, String tokenId) {
        String key = REFRESH_TOKEN_PREFIX + userId + ":" + tokenId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
