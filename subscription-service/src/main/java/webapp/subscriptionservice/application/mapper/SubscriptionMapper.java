package webapp.subscriptionservice.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;
import webapp.subscriptionservice.domain.model.SubscriptionHistory;
import webapp.subscriptionservice.domain.model.UserSubscription;

/**
 * Интерфейс для автоматического преобразования между domain.model и DTO
 **/
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    UserSubscriptionDto toUserSubscriptionDto(UserSubscription userSubscription);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "meta", source = "meta")
    @Mapping(target = "createdAt", source = "createdAt")
    SubscriptionHistoryDto toHistoryDto(SubscriptionHistory subscriptionHistory);
}
