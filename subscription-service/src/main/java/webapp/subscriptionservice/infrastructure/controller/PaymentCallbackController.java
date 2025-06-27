package webapp.subscriptionservice.infrastructure.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import ru.kata.project.model.PaymentStatus;
import webapp.subscriptionservice.application.util.LogSanitizer;
import webapp.subscriptionservice.domain.service.SubscriptionService;


/**
 * Контроллер для обработки коллбэков от внешних платежных шлюзов.
 * Этот контроллер предоставляет эндпоинт для приема входящих HTTP-уведомлений (вебхуков)
 * от платежных шлюзов (например, YooKassa) о статусе платежей. Он обрабатывает
 * уведомления и делегирует бизнес-логику в SubscriptionService.
 **/
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Платежные Колбэки (Сервер-сервер)",
        description = "Эндпоинт для обработки входящих HTTP-уведомлений (вебхуков) от платежных шлюзов (YooKassa).")
public class PaymentCallbackController {

    private final SubscriptionService subscriptionService;
    @SuppressFBWarnings("EI2")
    private final ObjectMapper objectMapper;

    /**
     * Метод для обработки входящих POST-запросов от YooKassa.
     * Этот эндпоинт принимает автоматические уведомления от YooKassa о статусе платежа.
     * Он не предназначен для прямого вызова клиентами.
     * Метод парсит JSON-тело запроса, извлекает ID платежа, статус и метаданные,
     * а затем делегирует дальнейшую обработку в SubscriptionService.
     *
     * @param payload JSON-тело запроса (коллбэка) от платежного шлюза.
     * @return ResponseEntity со статусом 200 OK в случае успешной обработки
     * или со статусом 500 INTERNAL_SERVER_ERROR в случае возникновения ошибок.
     * @throws JsonProcessingException Может возникнуть при некорректной обработке JSON-метаданных.
     * @throws Exception               Любые другие непредвиденные исключения во время обработки коллбэка.
     */
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

    @SuppressFBWarnings(value = "CRLF_INJECTION_LOGS")
    @Hidden
    public ResponseEntity<String> handleYookassaCallback(@RequestBody JsonNode payload) {
        log.info("Получен коллбэк от Yookassa: {}", LogSanitizer.sanitizeLogInput(payload.toPrettyString()));

        /** В Production здесь должна быть верификация подписи HMAC-SHA256
         * Headers: YooKassa-Signature
         * Можно реализовать отдельный фильтр или Interceptor
         * https://yookassa.ru/developers/api#callback_url_verification
         * Это КРИТИЧНО для безопасности, чтобы убедиться, что коллбэк пришел именно от YooKassa, а не от злоумышленника. **/

        try {
            final String event = payload.path("event").asText();
            final JsonNode objectNode = payload.path("object");
            final String paymentId = objectNode.path("id").asText();
            final String statusString = objectNode.path("status").asText();
            final JsonNode metadataNode = objectNode.path("metadata");

            log.info("Обработка события Yookassa: {}, payment ID: {}, status: {}",
                    LogSanitizer.sanitizeLogInput(event),
                    LogSanitizer.sanitizeLogInput(paymentId),
                    LogSanitizer.sanitizeLogInput(statusString));

            final PaymentStatus paymentStatus = PaymentStatus.fromString(statusString);
            String metadataJson = "";
            if (metadataNode != null && !metadataNode.isNull() && !metadataNode.isEmpty()) {
                try {
                    metadataJson = objectMapper.writeValueAsString(metadataNode);
                } catch (JsonProcessingException e) {
                    log.error("Ошибка при сериализации метаданных из коллбэка платежного шлюза для paymentId {}: {}",
                            LogSanitizer.sanitizeLogInput(paymentId),
                            LogSanitizer.sanitizeLogInput(e.getMessage()));
                    metadataJson = "{\"serialization_error\": \"" + LogSanitizer.sanitizeLogInput(e.getMessage()) + "\"}";
                }
            }
            subscriptionService.handlePaymentCallback(paymentId, paymentStatus, metadataJson);
            return ResponseEntity.ok("Коллбэк успешно обработан");

        } catch (Exception e) {
            log.error("Ошибка при обработке коллбэка от Yookassa: {}", LogSanitizer.sanitizeLogInput(e.getMessage()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка обработки коллбэка");
        }
    }

}
