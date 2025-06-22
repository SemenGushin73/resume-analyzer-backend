package webapp.subscriptionservice.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.model.PaymentProvider;
import ru.kata.project.model.SubscriptionStatus;
import ru.kata.project.model.SubscriptionType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Класс, представляющий текущую или прошлую подписку пользователя в доменной модели.
 * Содержит только бизнес-атрибуты и поведение, без привязки к конкретной технологии хранения данных.
 **/
@Data
@NoArgsConstructor
public class UserSubscription {

    private UUID id;
    private UUID userId;
    private SubscriptionType type;
    private SubscriptionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private PaymentProvider paymentProvider;
    private String externalPaymentId;
    private String paymentSessionUrl;
    private String qrCodeData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserSubscription(UUID userId, SubscriptionType type, SubscriptionStatus status,
                            PaymentProvider paymentProvider, String externalPaymentId,
                            String paymentSessionUrl, String qrCodeData) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.startedAt = LocalDateTime.now();
        this.paymentProvider = paymentProvider;
        this.externalPaymentId = externalPaymentId;
        this.paymentSessionUrl = paymentSessionUrl;
        this.qrCodeData = qrCodeData;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserSubscription(UUID id, UUID userId, SubscriptionType type, SubscriptionStatus status,
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

    public void activate(LocalDateTime newExpiresAt) {
        this.status = SubscriptionStatus.ACTIVE;
        this.expiresAt = newExpiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = SubscriptionStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
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
        return Objects.hash(id);
    }
}