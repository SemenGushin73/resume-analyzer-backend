package webapp.subscriptionservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.model.PaymentProvider;
import ru.kata.project.model.SubscriptionStatus;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность JPA для хранения информации о подписках пользователя в базе данных.
 * Этот класс содержит все JPA-аннотации, необходимые для ORM-маппинга,
 * и представляет собой персистентное представление доменной модели UserSubscription.
 */
@Entity
@Table(name = "user_subscriptions")
@Data
@NoArgsConstructor
public class UserSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider")
    private PaymentProvider paymentProvider;

    @Column(name = "external_payment_id")
    private String externalPaymentId;

    @Column(name = "payment_session_url")
    private String paymentSessionUrl;

    @Column(name = "qr_code_data")
    private String qrCodeData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserSubscriptionEntity(UUID id, UUID userId, SubscriptionType type, SubscriptionStatus status,
                                  LocalDateTime startedAt, LocalDateTime expiresAt, PaymentProvider paymentProvider,
                                  String externalPaymentId, String paymentSessionUrl, String qrCodeData,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.paymentProvider = paymentProvider;
        this.externalPaymentId = externalPaymentId;
        this.paymentSessionUrl = paymentSessionUrl;
        this.qrCodeData = qrCodeData;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}