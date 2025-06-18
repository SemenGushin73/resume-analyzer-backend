package webapp.subscriptionservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kata.project.model.SubscriptionStatus;
import webapp.subscriptionservice.domain.model.UserSubscription;

import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс для работы с активными подписками пользователей с методами для взаимодействия с БД
 **/
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {
    Optional<UserSubscription> findByUserId(UUID userId);

    Optional<UserSubscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    Optional<UserSubscription> findByExternalPaymentId(String externalPaymentId);
}
