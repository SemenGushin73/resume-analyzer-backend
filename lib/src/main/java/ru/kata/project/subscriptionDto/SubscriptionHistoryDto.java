package ru.kata.project.subscriptionDto;

import lombok.Data;
import ru.kata.project.model.SubscriptionEventType;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для передачи информации об истории событий, связанных с подпиской
 **/
@Data
public class SubscriptionHistoryDto {
    private UUID id;
    private UUID userId;
    private SubscriptionType type;
    private SubscriptionEventType eventType;
    private String meta;
    private LocalDateTime createdAt;
}