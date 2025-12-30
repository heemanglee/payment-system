package com.example.assignment.point.user.application;

import com.example.assignment.point.user.domain.service.UserService;
import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.user.dto.response.SignUpResponse;
import com.example.assignment.point.user.exception.RegistrationConcurrencyException;
import com.example.assignment.point.user.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationFacade {

    private final UserService userService;
    private final RedissonClient redissonClient;

    private final String PREFIX = "lock:signup:";

    @Retryable(
        retryFor = {
            TransientDataAccessException.class,
            CannotAcquireLockException.class,
            QueryTimeoutException.class,
            CannotCreateTransactionException.class,
            RegistrationConcurrencyException.class
        },
        noRetryFor = {
            DuplicateKeyException.class,
            DataIntegrityViolationException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2)
    )
    public SignUpResponse registerWithLock(SignUpRequest request) {
        RLock lock = redissonClient.getLock(PREFIX + request.email());

        try {
            boolean isLocked = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RegistrationConcurrencyException("Registration in progress for email: " + request.email());
            }

            return userService.create(request);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("System error occurred.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Recover
    public SignUpResponse recover(Exception e, SignUpRequest request) {
        log.error("[REGISTRATION-FINAL-FAILURE] Failed after 3 retries - email: {}, error: {}",
            request.email(), e.getMessage());
        throw new RegistrationFailedException("Registration failed. Please try again later.");
    }
}
