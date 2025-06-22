package webapp.subscriptionservice.infrastructure.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.kata.project.model.SubscriptionEventType;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность JPA для хранения истории событий, связанных с подписками, в базе данных.
 * Этот класс содержит все JPA-аннотации и используется для ORM-маппинга.
 **/
@Entity
@Table(name = "subscription_history")
@Data
@NoArgsConstructor
public class SubscriptionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private SubscriptionEventType eventType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode meta;

    public SubscriptionHistoryEntity(UUID id, UUID userId, SubscriptionType type, SubscriptionEventType eventType, JsonNode meta, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.eventType = eventType;
        this.meta = meta;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}