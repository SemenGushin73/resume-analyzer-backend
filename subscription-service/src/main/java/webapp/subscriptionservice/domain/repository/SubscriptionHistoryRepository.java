package webapp.subscriptionservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webapp.subscriptionservice.infrastructure.persistence.entity.SubscriptionHistoryEntity;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс репозитория для взаимодействия с БД и получения/сохранения записей истории подписок.
 */
@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistoryEntity, UUID> {
    List<SubscriptionHistoryEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
