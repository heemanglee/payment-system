package com.example.assignment.point.user.domain.service;

import com.example.assignment.point.outbox.domain.entity.EventStatus;
import com.example.assignment.point.outbox.domain.repository.OutboxRepository;
import com.example.assignment.point.user.application.UserRegistrationFacade;
import com.example.assignment.point.user.domain.repository.UserRepository;
import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.wallet.domain.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"wallet-create-topic"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"}
)
@Slf4j
class UserWalletHighConcurrencyTest {

    @Autowired
    private UserRegistrationFacade userRegistrationFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        cleanupRedis();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // Redis 데이터 먼저 정리
        cleanupRedis();

        // Kafka consumer가 비동기로 지갑을 생성 중일 수 있으므로 재시도 로직 추가
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                walletRepository.deleteAllInBatch();
                outboxRepository.deleteAllInBatch();
                userRepository.deleteAllInBatch();
                return;
            } catch (Exception e) {
                log.warn("[TEARDOWN] Retry {}/{} - Delete failed: {}", i + 1, maxRetries, e.getMessage());
                Thread.sleep(500);
            }
        }
        log.error("[TEARDOWN] Failed to clean up after {} retries", maxRetries);
    }

    private void cleanupRedis() {
        try {
            var connection = redisTemplate.getConnectionFactory().getConnection();
            connection.serverCommands().flushDb();
            log.info("[CLEANUP] Redis data flushed successfully");
        } catch (Exception e) {
            log.warn("[CLEANUP] Redis flush failed: {}", e.getMessage());
        }
    }

    @Test
    @DisplayName("1000명의 사용자가 동시에 가입했을 때, 1000개의 지갑이 모두 생성되어야 한다")
    void highConcurrencyRegistrationTest() throws InterruptedException {
        // given
        int userCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(userCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        log.info("[TEST-START] Starting registration for {} users", userCount);

        // when
        for (int i = 0; i < userCount; i++) {
            final String email = "test" + i + "@test.com";
            final SignUpRequest request = new SignUpRequest(email, "password123", "user" + i);
            executorService.execute(() -> {
                try {
                    userRegistrationFacade.registerWithLock(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.warn("[TEST-ERROR] Registration failed for {}: {}", email, e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 요청 완료 대기
        boolean allRequestsCompleted = latch.await(60, TimeUnit.SECONDS);
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(allRequestsCompleted)
                .as("All registration requests should complete within 30 seconds")
                .isTrue();
        assertThat(terminated)
                .as("ExecutorService should terminate within 10 seconds")
                .isTrue();

        log.info("[TEST-STEP] All registration requests submitted. Success: {}, Fail: {}", successCount.get(), failCount.get());

        // User creation check
        assertThat(userRepository.count()).isEqualTo(userCount);
        assertThat(successCount.get()).isEqualTo(userCount);

        // 비동기 처리를 위해 대기 (Kafka consumer가 지갑을 생성할 때까지)
        log.info("[TEST-STEP] Waiting for wallets to be created via Kafka...");

        boolean allWalletsCreated = false;
        int maxRetries = 60;
        for (int i = 0; i < maxRetries; i++) {
            long walletCount = walletRepository.count();
            long publishedCount = outboxRepository.findByStatus(EventStatus.PUBLISHED).size();

            log.info("[TEST-POLLING] Attempt {}/{} - Wallet Count: {}, Published Event Count: {}",
                    i + 1, maxRetries, walletCount, publishedCount);

            if (walletCount == userCount && publishedCount == userCount) {
                allWalletsCreated = true;
                break;
            }
            Thread.sleep(500);
        }

        // Wallet creation and Outbox status check
        assertThat(allWalletsCreated)
                .as("All wallets should be created and all events should be published")
                .isTrue();

        assertThat(walletRepository.count()).isEqualTo(userCount);
        assertThat(outboxRepository.findByStatus(EventStatus.INITIATED)).isEmpty();

        log.info("[TEST-END] Successfully verified {} users and {} wallets", userCount, userCount);
    }
}
