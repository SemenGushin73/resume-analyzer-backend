package webapp.subscriptionservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webapp.subscriptionservice.domain.model.SubscriptionHistory;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс с методами, связанными с историей подписок, для взаимодействия с БД
 **/
@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, UUID> {
    List<SubscriptionHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
