package com.example.assignment.point.user.domain.service;

import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.user.dto.response.SignUpResponse;
import com.example.assignment.point.user.domain.entity.User;
import com.example.assignment.point.user.domain.entity.UserRole;
import com.example.assignment.point.user.domain.entity.UserStatus;
import com.example.assignment.point.user.exception.DuplicateEmailException;
import com.example.assignment.point.user.domain.repository.UserRepository;
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

    @Transactional
    public SignUpResponse create(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
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

        return SignUpResponse.fromEntity(savedUser);
    }
}
