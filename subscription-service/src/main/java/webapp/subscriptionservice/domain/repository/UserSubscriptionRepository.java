package webapp.subscriptionservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kata.project.model.SubscriptionStatus;
import webapp.subscriptionservice.infrastructure.persistence.entity.UserSubscriptionEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс репозитория для взаимодействия с БД для управления текущими и прошлыми подписками пользователей.
 */
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UUID> {
    Optional<UserSubscriptionEntity> findByUserId(UUID userId);

    Optional<UserSubscriptionEntity> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    Optional<UserSubscriptionEntity> findByExternalPaymentId(String externalPaymentId);
}
