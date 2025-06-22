package webapp.subscriptionservice.application.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.kata.project.subscriptionDto.SubscriptionMetaDto;

import java.util.UUID;

/**
 * Вспомогательный класс для обработки сериализации/десериализации JSON, связанной с метаданными подписки.
 * Он инкапсулирует использование ObjectMapper, делая сервисную логику чище.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionMetaJsonHelper {

    private final ObjectMapper objectMapper;

    /**
     * Создает JsonNode из SubscriptionMetaDto.
     */
    public JsonNode createSubscriptionMetaNode(String message, String paymentId, String subscriptionType, String confirmationUrl) {
        final SubscriptionMetaDto metaDto = new SubscriptionMetaDto();
        metaDto.setMessage(message);
        metaDto.setPaymentId(paymentId);
        metaDto.setSubscriptionType(subscriptionType);
        metaDto.setConfirmationUrl(confirmationUrl);
        return objectMapper.valueToTree(metaDto);
    }

    /**
     * Подготавливает ObjectNode с метаданными коллбэка от платежного шлюза.
     * Включает ID платежа, статус и внешние метаданные, с обработкой ошибок парсинга JSON.
     * **Вся логика добавления ID подписки и обработки ошибок парсинга метаданных
     * теперь находится внутри этого метода.**
     */
    public ObjectNode prepareCallbackMetaNode(String paymentId, String status, String metadataJson, UUID subscriptionId) {
        final ObjectNode metaNode = objectMapper.createObjectNode();
        metaNode.put("paymentId", paymentId);
        metaNode.put("statusFromGateway", status);

        if (subscriptionId != null) {
            metaNode.put("subscriptionId", subscriptionId.toString());
        }

        if (metadataJson != null && !metadataJson.isEmpty()) {
            try {
                final JsonNode externalMeta = objectMapper.readTree(metadataJson);
                metaNode.set("externalData", externalMeta);
            } catch (JsonProcessingException e) {
                log.warn("Ошибка парсинга метаданных коллбэка для paymentId {}: {}", paymentId, e.getMessage());
                metaNode.put("parseError", "Ошибка парсинга metadata");
                metaNode.put("rawData", metadataJson);
            }
        }
        return metaNode;
    }
}