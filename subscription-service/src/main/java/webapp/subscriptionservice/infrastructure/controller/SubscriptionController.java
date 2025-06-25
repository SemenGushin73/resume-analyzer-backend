package webapp.subscriptionservice.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.project.exception.ResourceNotFoundException;
import ru.kata.project.subscriptionDto.CreateSubscriptionRequest;
import ru.kata.project.subscriptionDto.PaymentSessionResponseDto;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;
import webapp.subscriptionservice.application.util.LogSanitizer;
import webapp.subscriptionservice.domain.service.SubscriptionService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Контроллер для взаимодействия с сервисом подписок.
 * Предоставляет внутренние API-эндпоинты для управления подписками пользователей,
 * включая создание, отмену и получение информации о подписках.
 * Эндпоинты используются через API Gateway.
 **/
@RestController
@RequestMapping("/internal/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Управление Подписками (Внутреннее API)", description = "Эндпоинты для создания, "
        + "отмены и получения информации о подписках. Доступны через API Gateway.")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Эндпоинт для создания новой подписки и инициирования платежной сессии.
     * Принимает запрос на создание подписки, проверяет наличие активных подписок у пользователя,
     * взаимодействует с платежным шлюзом для создания платежной сессии и возвращает
     * информацию для клиента, необходимую для завершения оплаты.
     *
     * @param request Объект CreateSubscriptionRequest, содержащий ID пользователя и тип подписки.
     * @return ResponseEntity с PaymentSessionResponseDto (статус 201 Created)
     * в случае успеха или соответствующим статусом ошибки (400, 503, 504, 500).
     */
    @PostMapping("/create")
    @Operation(summary = "Создать новую подписку и платежную сессию",
            description = "Инициирует процесс создания подписки для пользователя, "
                    + "возвращает URL для оплаты или QR-код. "
                    + "Статус подписки будет 'INACTIVE' до успешного коллбэка от платежного шлюза.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Подписка создана, возвращены данные платежной сессии",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentSessionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос или у пользователя уже есть активная подписка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"У пользователя уже есть активная подписка\"}"))),
            @ApiResponse(responseCode = "503", description = "Платежный шлюз временно недоступен"),
            @ApiResponse(responseCode = "504", description = "Платежный шлюз не ответил в срок"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> createSubscription(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Детали запроса на создание подписки",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateSubscriptionRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody @Valid CreateSubscriptionRequest request) {

        log.info("Получен запрос на создание подписки для пользователя userId: {}, type: {}",
                LogSanitizer.sanitizeLogInput(request.getUserId().toString()), LogSanitizer.sanitizeLogInput(request.getType().name()));
        try {
            final PaymentSessionResponseDto response = subscriptionService.createSubscription(request.getUserId(), request.getType());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ExecutionException e) {
            log.error("ExecutionException во время создания подписки для пользователя userId: {}", LogSanitizer.sanitizeLogInput(request.getUserId().toString()), e);
            final Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("Платежный шлюз не ответил в срок. Пожалуйста попробуйте позже.");
            } else if (cause != null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Платежный шлюз временно недоступен: " + LogSanitizer.sanitizeLogInput(cause.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Неожиданная ошибка во время создания подписки для пользователя.");
        } catch (InterruptedException e) {
            log.error("InterruptedException во время создания подписки для пользователя userId: {}", LogSanitizer.sanitizeLogInput(request.getUserId().toString()), e);
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Создание подписки прервано.");
        } catch (IllegalArgumentException e) {
            log.warn("Неверный запрос на создание подписки для пользователя userId: {}: {}", LogSanitizer.sanitizeLogInput(request.getUserId().toString()), LogSanitizer.sanitizeLogInput(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка при создании подписки для пользователя userId: {}", LogSanitizer.sanitizeLogInput(request.getUserId().toString()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Возникла неожиданная ошибка: " + LogSanitizer.sanitizeLogInput(e.getMessage()));
        }
    }

    /**
     * Эндпоинт для отмены активной подписки пользователя.
     * Находит и деактивирует текущую активную подписку для указанного пользователя.
     * Если подписка не найдена, возвращает статус 404.
     *
     * @param userId UUID пользователя, чья подписка должна быть отменена.
     * @return ResponseEntity со статусом 200 OK в случае успешной отмены
     * или соответствующим статусом ошибки (404, 503, 504, 500).
     */
    @PostMapping("/cancel")
    @Operation(summary = "Отменить активную подписку пользователя",
            description = "Отменяет текущую активную подписку для указанного пользователя. "
                    + "Возвращает статус 200 в случае успеха или 404, если подписка не найдена.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подписка успешно отменена",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Подписка успешно создана"))),
            @ApiResponse(responseCode = "404", description = "Активная подписка не найдена для пользователя",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string",
                            example = "Активная подписка не найдена для пользователя: [userId]"))),
            @ApiResponse(responseCode = "503", description = "Платежный шлюз временно недоступен для отмены"),
            @ApiResponse(responseCode = "504", description = "Платежный шлюз не ответил в срок во время отмены")
    })
    public ResponseEntity<String> cancelSubscription(
            @Parameter(in = ParameterIn.QUERY, description = "UUID пользователя, чья подписка должна быть отменена",
                    required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @RequestParam UUID userId) {
        log.info("Получен запрос на отмену подписки для пользователя userId: {}", LogSanitizer.sanitizeLogInput(userId.toString()));
        try {
            subscriptionService.cancelSubscription(userId);
            return ResponseEntity.ok("Подписка успешно отменена.");
        } catch (ResourceNotFoundException e) {
            log.warn("Не найдена активная подписка для отмены для пользователя {}", LogSanitizer.sanitizeLogInput(userId.toString()));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ExecutionException e) {
            log.error("ExecutionException во время отмены подписки для пользователя userId: {}", LogSanitizer.sanitizeLogInput(userId.toString()), e);
            final Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                        .body("Платежный шлюз не ответил вовремя при отмене подписки. Пожалуйста, попробуйте позже.");
            } else if (cause != null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Платежный шлюз временно не доступен для отмены: "
                        + LogSanitizer.sanitizeLogInput(cause.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("При отмене подписки возникла непредвиденная ошибка.");
        } catch (InterruptedException e) {
            log.error("InterruptedException во время отмены подписки для пользователя userId: {}", LogSanitizer.sanitizeLogInput(userId.toString()), e);
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Отмена подписки прервана.");
        } catch (Exception e) {
            log.error("Ошибка при отмене подписки для пользователя userId: {}", LogSanitizer.sanitizeLogInput(userId.toString()), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Возникла непредвиденная ошибка: " + LogSanitizer.sanitizeLogInput(e.getMessage()));
        }
    }

    /**
     * Эндпоинт для получения истории подписок пользователя.
     * Возвращает список всех прошлых и текущих подписок для указанного пользователя,
     * отсортированных по дате создания в обратном хронологическом порядке (от новой к старой).
     *
     * @param userId UUID пользователя, чью историю подписок необходимо получить.
     * @return ResponseEntity со списком SubscriptionHistoryDto (статус 200 OK)
     * или статусом 500 INTERNAL_SERVER_ERROR в случае внутренней ошибки.
     */
    @GetMapping("/history")
    @Operation(summary = "Получить историю подписок пользователя",
            description = "Возвращает список всех прошлых и текущих подписок для указанного пользователя, "
                    + "отсортированных по дате создания в обратном порядке.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список записей истории подписок",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = SubscriptionHistoryDto.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<List<SubscriptionHistoryDto>> getSubscriptionHistory(
            @Parameter(in = ParameterIn.QUERY, description = "UUID пользователя, чью историю подписок необходимо получить",
                    required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @RequestParam UUID userId) {
        log.info("Получен запрос на получение истории подписок для пользователя userId: {}", LogSanitizer.sanitizeLogInput(userId.toString()));
        final List<SubscriptionHistoryDto> history = subscriptionService.getSubscriptionHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Эндпоинт для получения текущей активной подписки пользователя.
     * Возвращает детали активной подписки пользователя, если она имеется.
     * Если активная подписка не найдена, возвращает статус 404 Not Found.
     *
     * @param userId UUID пользователя, чью активную подписку необходимо получить.
     * @return ResponseEntity с UserSubscriptionDto (статус 200 OK)
     * или статусом 404 Not Found, если активная подписка не найдена.
     */
    @GetMapping("/active")
    @Operation(summary = "Получить активную подписку пользователя",
            description = "Возвращает детали текущей активной подписки пользователя, если таковая имеется.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Активная подписка найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserSubscriptionDto.class))),
            @ApiResponse(responseCode = "404", description = "Активная подписка не найдена для пользователя",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string",
                            example = "Активная подписка не найдена для пользователя: [userId]"))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<UserSubscriptionDto> getActiveSubscription(
            @Parameter(in = ParameterIn.QUERY, description = "UUID пользователя, чью активную подписку необходимо получить",
                    required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @RequestParam UUID userId) {
        log.info("Получен запрос на информацию об активной подписке для пользователя userId: {}", LogSanitizer.sanitizeLogInput(userId.toString()));
        return subscriptionService.getActiveSubscription(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
