package webapp.subscriptionservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import webapp.subscriptionservice.domain.model.UserSubscription;
import webapp.subscriptionservice.infrastructure.persistence.entity.UserSubscriptionEntity;

/**
 * Компонент-маппер для преобразования между доменной моделью UserSubscription
 * и сущностью персистентности UserSubscriptionEntity.
 **/
@Component
public class UserSubscriptionMapper {

    public UserSubscriptionEntity toEntity(UserSubscription domainModel) {
        if (domainModel == null) {
            return null;
        }
        final UserSubscriptionEntity entity = new UserSubscriptionEntity();
        if (domainModel.getId() != null) {
            entity.setId(domainModel.getId());
        }

        entity.setUserId(domainModel.getUserId());
        entity.setType(domainModel.getType());
        entity.setStatus(domainModel.getStatus());
        entity.setStartedAt(domainModel.getStartedAt());
        entity.setExpiresAt(domainModel.getExpiresAt());
        entity.setPaymentProvider(domainModel.getPaymentProvider());
        entity.setExternalPaymentId(domainModel.getExternalPaymentId());
        entity.setPaymentSessionUrl(domainModel.getPaymentSessionUrl());
        entity.setQrCodeData(domainModel.getQrCodeData());
        return entity;
    }

    public UserSubscription toDomainModel(UserSubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserSubscription(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getExpiresAt(),
                entity.getPaymentProvider(),
                entity.getExternalPaymentId(),
                entity.getPaymentSessionUrl(),
                entity.getQrCodeData(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}