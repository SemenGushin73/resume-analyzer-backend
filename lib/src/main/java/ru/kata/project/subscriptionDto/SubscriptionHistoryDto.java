package ru.kata.project.subscriptionDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.model.SubscriptionEventType;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для передачи информации об истории событий, связанных с подпиской
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionHistoryDto {
    private UUID id;
    private UUID userId;
    private SubscriptionType type;
    private SubscriptionEventType eventType;
    private SubscriptionMetaDto meta;
    private LocalDateTime createdAt;
}