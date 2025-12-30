package com.example.assignment.point.user.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisRegistrationService implements RegistrationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String KEY = "registered_emails";

    public void addEmail(String email) {
        redisTemplate.opsForSet().add(KEY, email);
    }

    public boolean isEmailExists(String email) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(KEY, email));
    }

}
