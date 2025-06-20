package webapp.subscriptionservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.model.PaymentProvider;
import ru.kata.project.model.SubscriptionStatus;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Класс с информацией об активных и прошлых подписках пользователя
 **/
@Entity
@Table(name = "user_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription {

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
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider;

    private String externalPaymentId;

    private String paymentSessionUrl;
    private String qrCodeData;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserSubscription that = (UserSubscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        } else {
            return 0;
        }
    }
}
