package com.example.assignment.point.user.domain.service;

import com.example.assignment.point.outbox.domain.entity.EventStatus;
import com.example.assignment.point.outbox.domain.entity.OutboxEvent;
import com.example.assignment.point.outbox.domain.repository.OutboxRepository;
import com.example.assignment.point.user.domain.entity.User;
import com.example.assignment.point.user.domain.entity.UserRole;
import com.example.assignment.point.user.domain.entity.UserStatus;
import com.example.assignment.point.user.domain.repository.UserRepository;
import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.user.dto.response.SignUpResponse;
import com.example.assignment.point.user.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxRepository outboxRepository;
    private final RedisRegistrationService registrationService;

    @Transactional
    public SignUpResponse create(SignUpRequest request) {
        boolean emailExists = registrationService.isEmailExists(request.email());
        if (emailExists) {
            throw new DuplicateEmailException("Email already exists: " + request.email());
        }

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .username(request.username())
            .userStatus(UserStatus.ACTIVE)
            .role(UserRole.USER)
            .build();

        User savedUser = userRepository.save(user);

        registrationService.addEmail(request.email());

        // 사용자의 지갑 생성
        OutboxEvent outbox = OutboxEvent.builder()
            .status(EventStatus.INITIATED)
            .userId(user.getId())
            .build();
        outboxRepository.save(outbox);

        return SignUpResponse.fromEntity(savedUser);
    }
}
