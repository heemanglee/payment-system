package com.example.assignment.point.wallet.application;

import com.example.assignment.point.user.domain.entity.User;
import com.example.assignment.point.user.domain.repository.UserRepository;
import com.example.assignment.point.user.exception.UserNotFoundException;
import com.example.assignment.point.wallet.domain.entity.Wallet;
import com.example.assignment.point.wallet.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletConsumer {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "wallet-create-topic", groupId = "wallet-service-group")
    public void consume(String userIdPayload) {
        Long userId = Long.parseLong(userIdPayload);
        log.info("[WALLET-CONSUME] Received wallet creation event for userId: {}", userId);

        if (walletRepository.existsByUserId(userId)) {
            log.warn("[WALLET-CONSUME] Wallet already exists for userId: {}. Skipping.", userId);
            return;
        }

        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for userId: " + userId));

            Wallet newWallet = Wallet.builder()
                .user(user)
                .balance(0L)
                .build();
            walletRepository.save(newWallet);

            log.info("[WALLET-CONSUME] Successfully created wallet for userId: {}", userId);
        } catch (DataIntegrityViolationException e) {
            log.error("[WALLET-CONSUME] Duplicate wallet creation attempt for userId: {}", userId);
        }
    }
}
