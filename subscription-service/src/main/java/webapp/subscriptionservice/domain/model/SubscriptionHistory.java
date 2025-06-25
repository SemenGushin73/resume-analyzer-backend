package webapp.subscriptionservice.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.model.SubscriptionEventType;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Класс, представляющий историю событий, связанных с подписками, в доменной модели.
 * Он содержит только бизнес-атрибуты и поведение, без привязки к конкретной технологии хранения данных.
 */
@Data
@NoArgsConstructor
public class SubscriptionHistory {

    private UUID id;
    private UUID userId;
    private SubscriptionType type;
    private SubscriptionEventType eventType;
    private JsonNode meta;
    private LocalDateTime createdAt;

    public SubscriptionHistory(UUID userId, SubscriptionType type, SubscriptionEventType eventType, JsonNode meta) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.eventType = eventType;
        this.meta = meta;
        this.createdAt = LocalDateTime.now();
    }

    public SubscriptionHistory(UUID id, UUID userId, SubscriptionType type, SubscriptionEventType eventType, JsonNode meta, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.eventType = eventType;
        this.meta = meta;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SubscriptionHistory that = (SubscriptionHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}