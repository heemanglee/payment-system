package com.example.assignment.point.user.application;

import com.example.assignment.point.user.domain.service.UserService;
import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.user.dto.response.SignUpResponse;
import com.example.assignment.point.user.exception.RegistrationConcurrencyException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegistrationFacade {

    private final UserService userService;
    private final RedissonClient redissonClient;

    private final String PREFIX = "lock:signup:";

    public SignUpResponse registerWithLock(SignUpRequest request) {
        RLock lock = redissonClient.getLock(PREFIX + request.email());

        try {
            boolean isLocked = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RegistrationConcurrencyException("가입이 진행 중인 이메일 입니다: " + request.email());
            }

            return userService.create(request);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("시스템 오류가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
