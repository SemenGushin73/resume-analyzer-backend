package webapp.subscriptionservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import webapp.subscriptionservice.domain.model.SubscriptionHistory;
import webapp.subscriptionservice.infrastructure.persistence.entity.SubscriptionHistoryEntity;

import java.time.LocalDateTime;

/**
 * Компонент-маппер для преобразования между доменной моделью SubscriptionHistory
 * и сущностью персистентности SubscriptionHistoryEntity.
 **/
@Component
public class SubscriptionHistoryMapper {

    public SubscriptionHistoryEntity toEntity(SubscriptionHistory domainModel) {
        if (domainModel == null) {
            return null;
        }
        final SubscriptionHistoryEntity entity = new SubscriptionHistoryEntity();
        entity.setUserId(domainModel.getUserId());
        entity.setType(domainModel.getType());
        entity.setEventType(domainModel.getEventType());
        entity.setMeta(domainModel.getMeta());
        if (domainModel.getCreatedAt() != null) {
            entity.setCreatedAt(domainModel.getCreatedAt());
        } else {
            entity.setCreatedAt(LocalDateTime.now());
        }

        return entity;
    }

    public SubscriptionHistory toDomainModel(SubscriptionHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SubscriptionHistory(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getEventType(),
                entity.getMeta(),
                entity.getCreatedAt()
        );
    }
}