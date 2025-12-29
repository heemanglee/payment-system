package com.example.assignment.point.outbox.application;

import com.example.assignment.point.outbox.domain.entity.EventStatus;
import com.example.assignment.point.outbox.domain.entity.OutboxEvent;
import com.example.assignment.point.outbox.domain.repository.OutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    public void publishEvents() {
        List<OutboxEvent> events = outboxRepository.findByStatus(EventStatus.INITIATED);

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send("wallet-create-topic", String.valueOf(event.getUserId()));

                // 성공 시 상태 업데이트
                event.markAsPublished();
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish event: {}", event.getId(), e);
            }
        }
    }
}