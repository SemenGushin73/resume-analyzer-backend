package webapp.subscriptionservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webapp.subscriptionservice.infrastructure.persistence.entity.SubscriptionHistoryEntity;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс с методами, связанными с историей подписок, для взаимодействия с БД.
 * Теперь работает с сущностью персистентности SubscriptionHistoryEntity.
 **/
@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistoryEntity, UUID> {
    List<SubscriptionHistoryEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
