package webapp.subscriptionservice.infrastructure.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webapp.subscriptionservice.domain.service.SubscriptionService;

/**
 * Контроллер для обработки коллбэков от внешних платежных шлюзов
 **/
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Платежные Колбэки (Сервер-сервер)", description = "Эндпоинт для обработки входящих HTTP-уведомлений (вебхуков) от платежных шлюзов (YooKassa).")
public class PaymentCallbackController {

    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    @PostMapping("/callback")
    @Operation(summary = "Обработка коллбэков от платежного шлюза (Yookassa)",
            description = "Принимает и обрабатывает автоматические уведомления от Yookassa о статусе платежа. " +
                    "Этот эндпоинт не предназначен для прямого вызова клиентами. " +
                    "**ВНИМАНИЕ: В production должна быть реализована верификация подписи YooKassa-Signature для безопасности!**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Коллбэк успешно обработан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос или необработанный тип события"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера при обработке коллбэка")
    })

    /** Метод для обработки входящих POST-запросов от YooKassa. **/
    @Hidden
    public ResponseEntity<String> handleYookassaCallback(@RequestBody JsonNode payload) {
        log.info("Получен коллбэк от Yookassa: {}", payload.toPrettyString());

        /** В Production здесь должна быть верификация подписи HMAC-SHA256
         * Headers: YooKassa-Signature
         * Можно реализовать отдельный фильтр или Interceptor
         * https://yookassa.ru/developers/api#callback_url_verification
         * Это КРИТИЧНО для безопасности, чтобы убедиться, что коллбэк пришел именно от YooKassa, а не от злоумышленника. **/

        try {
            final String event = payload.path("event").asText();
            final JsonNode objectNode = payload.path("object");
            final String paymentId = objectNode.path("id").asText();
            final String status = objectNode.path("status").asText();
            final JsonNode metadataNode = objectNode.path("metadata");

            log.info("Обработка события Yookassa: {}, payment ID: {}, status: {}", event, paymentId, status);

            String metadataJson = "";
            if (metadataNode != null && !metadataNode.isNull() && !metadataNode.isEmpty()) {
                try {
                    metadataJson = objectMapper.writeValueAsString(metadataNode);
                } catch (JsonProcessingException e) {
                    log.error("Ошибка при сериализации метаданных из коллбэка платежного шлюза для paymentId {}: {}", paymentId, e.getMessage());
                    metadataJson = "{\"serialization_error\": \"" + e.getMessage() + "\"}";
                }
            }
            subscriptionService.handlePaymentCallback(paymentId, status, metadataJson);
            return ResponseEntity.ok("Коллбэк успешно обработан");

        } catch (Exception e) {
            log.error("Ошибка при обработке коллбэка от Yookassa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка обработки коллбэка");
        }
    }
}
