package webapp.subscriptionservice.application.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.SubscriptionMetaDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;
import webapp.subscriptionservice.domain.model.SubscriptionHistory;
import webapp.subscriptionservice.domain.model.UserSubscription;

/**
 * Интерфейс для автоматического преобразования между domain.model и DTO
 **/
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "meta", expression = "java(mapMeta(history.getMeta()))")
    SubscriptionHistoryDto toHistoryDto(SubscriptionHistory history);

    UserSubscriptionDto toUserSubscriptionDto(UserSubscription subscription);

    default SubscriptionMetaDto mapMeta(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.treeToValue(jsonNode, SubscriptionMetaDto.class);
        } catch (JsonProcessingException e) {
            final SubscriptionMetaDto dto = new SubscriptionMetaDto();
            dto.setError("Ошибка конвертации метаданных");
            dto.setDetails(jsonNode.toString());
            return dto;
        }
    }
}