package com.example.assignment.point.outbox.domain.repository;

import com.example.assignment.point.outbox.domain.entity.EventStatus;
import com.example.assignment.point.outbox.domain.entity.OutboxEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatus(EventStatus eventStatus);
}
