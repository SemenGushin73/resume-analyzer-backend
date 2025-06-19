package webapp.subscriptionservice.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.kata.project.model.SubscriptionEventType;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Класс для хранения истории событий, связанных с подписками
 **/
@Entity
@Table(name = "subscription_history")
@Data
@NoArgsConstructor
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionEventType eventType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode meta;

    public SubscriptionHistory(UUID userId, SubscriptionType type, SubscriptionEventType eventType, JsonNode meta) {
        this.userId = userId;
        this.type = type;
        this.eventType = eventType;
        this.meta = meta;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionHistory that = (SubscriptionHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 0;
    }
}
