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
 * Интерфейс для автоматического преобразования между доменными моделями (domain.model) и объектами передачи данных (DTO).
 * Использует MapStruct
 */
@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Преобразует доменную модель SubscriptionHistory в DTO SubscriptionHistoryDto.
     *
     * @param history Доменная модель истории подписки, которую необходимо преобразовать.
     * @return DTO SubscriptionHistoryDto, содержащее информацию об истории подписки.
     */
    @Mapping(target = "meta", expression = "java(mapMeta(history.getMeta()))")
    SubscriptionHistoryDto toHistoryDto(SubscriptionHistory history);

    /**
     * Преобразует доменную модель UserSubscription в DTO UserSubscriptionDto.
     *
     * @param subscription Доменная модель подписки пользователя, которую необходимо преобразовать.
     * @return DTO UserSubscriptionDto, содержащее информацию о подписке пользователя.
     */
    UserSubscriptionDto toUserSubscriptionDto(UserSubscription subscription);

    /**
     * Вспомогательный метод для маппинга JsonNode метаданных в SubscriptionMetaDto.
     * Обрабатывает случаи, когда jsonNode равен null, возвращая DTO с сообщением об ошибке.
     * Пытается преобразовать JsonNode в SubscriptionMetaDto с помощью ObjectMapper.
     * В случае JsonProcessingException возвращает DTO с информацией об ошибке конвертации.
     *
     * @param jsonNode Исходный JsonNode, содержащий метаданные.
     * @return SubscriptionMetaDto, представляющий преобразованные метаданные,
     * или DTO с информацией об ошибке, если преобразование не удалось или jsonNode был null.
     */
    default SubscriptionMetaDto mapMeta(JsonNode jsonNode) {
        if (jsonNode == null) {
            final SubscriptionMetaDto dto = new SubscriptionMetaDto();
            dto.setError("Метаданные отсутствуют");
            dto.setDetails("Исходный JsonNode был null.");
            return dto;
        }

        try {
            return OBJECT_MAPPER.treeToValue(jsonNode, SubscriptionMetaDto.class);
        } catch (JsonProcessingException e) {
            final SubscriptionMetaDto dto = new SubscriptionMetaDto();
            dto.setError("Ошибка конвертации метаданных");
            dto.setDetails(jsonNode.toString() + " | Ошибка: " + e.getMessage());
            return dto;
        }
    }
}