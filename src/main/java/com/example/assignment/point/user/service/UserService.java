package com.example.assignment.point.user.service;

import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.user.dto.request.SignupTemplate;
import com.example.assignment.point.user.dto.response.CreateUserResponse;
import com.example.assignment.point.user.entity.User;
import com.example.assignment.point.user.entity.UserRole;
import com.example.assignment.point.user.entity.UserStatus;
import com.example.assignment.point.user.exception.DuplicateEmailException;
import com.example.assignment.point.user.exception.InvalidVerificationCodeException;
import com.example.assignment.point.user.exception.VerificationCodeExpiredException;
import com.example.assignment.point.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public String register(SignUpRequest request) {
        // 이메일 중복 여부 확인 (이미 가입된 이메일인지)
        boolean exists = userRepository.existsByEmail(request.email());

        String key = String.format("signup:%s", request.email());
        Object object = redisTemplate.opsForValue().get(key);

        if (exists || object != null) {
            throw new DuplicateEmailException("Email already exists: " + request.email());
        }

        // 인증 코드 생성
        String verificationCode = generateVerificationCode();

        SignupTemplate signUpDto = new SignupTemplate(
            request.email(),
            request.password(),
            request.username(),
            verificationCode
        );

        // redis에 클라이언트가 입력한 정보 저장, TTL=3분
        redisTemplate.opsForValue().set(key, signUpDto, 3, TimeUnit.MINUTES);

        // 인증 코드 return
        return verificationCode;
    }

    @Transactional
    public CreateUserResponse create(SignUpRequest request) {
        String key = String.format("signup:%s", request.email());
        Object data = redisTemplate.opsForValue().get(key);
        SignupTemplate verifyData = objectMapper.convertValue(data, SignupTemplate.class);

        // 인증 정보 조회 실패
        if (verifyData == null) {
            throw new VerificationCodeExpiredException(String.format("Verification code expired: %s", request.email()));
        }

        // 인증 코드 불일치
        if (!verifyData.verificationCode().equals(request.verificationCode())) {
            throw new InvalidVerificationCodeException(
                String.format("Verification code mismatch: %s", request.verificationCode()));
        }

        // 사용자 생성
        User user = User.builder()
            .email(request.email())
            .password(request.password())
            .username(request.username())
            .userStatus(UserStatus.ACTIVE)
            .role(UserRole.USER)
            .build();
        User savedUser = userRepository.save(user);

        return CreateUserResponse.fromEntity(savedUser);
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;  // 100000 ~ 999999
        return String.valueOf(code);
    }
}
