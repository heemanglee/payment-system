package com.example.assignment.point.user.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.assignment.point.user.application.UserRegistrationFacade;
import com.example.assignment.point.user.domain.repository.UserRepository;
import com.example.assignment.point.user.dto.request.SignUpRequest;
import com.example.assignment.point.user.exception.DuplicateEmailException;
import com.example.assignment.point.user.exception.RegistrationConcurrencyException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRegistrationConcurrencyTest {

    @Autowired
    private UserRegistrationFacade userRegistrationFacade;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동일한 이메일로 동시에 2명이 가입 시도 시, 1명만 성공하고 1명은 중복 에러가 발생해야 한다")
    void concurrencyRegistrationTest() throws InterruptedException {
        // given
        String email = "test@test.com";
        SignUpRequest request = new SignUpRequest(email, "qwerasdf", "hope");

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    userRegistrationFacade.registerWithLock(request);
                    successCount.incrementAndGet();
                } catch (DuplicateEmailException | RegistrationConcurrencyException e) {
                    failCount.incrementAndGet();
                    System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 마칠 때까지 대기

        // then
        assertThat(successCount.get()).isEqualTo(1); // 가입 성공은 단 1건
        assertThat(failCount.get()).isEqualTo(1);    // 실패(중복 혹은 락 획득 실패)는 1건
    }
}