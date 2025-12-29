package com.example.assignment.point.outbox.domain.entity;

import com.example.assignment.point.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@Getter
public class OutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Builder
    public OutboxEvent(Long userId, EventStatus status) {
        this.userId = userId;
        this.status = status;
    }

    public void markAsPublished() {
        this.status = EventStatus.PUBLISHED;
    }
}
