package ru.kata.project.infrastructure.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kata.project.application.service.KafkaConsumerService;
import ru.kata.project.application.service.KafkaProducerService;
import ru.kata.project.domain.service.ApiGatewayService;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.ResumeScoreResult;
import ru.kata.project.kafka.config.KafkaTopics;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.subscriptionDto.CreateSubscriptionRequest;
import ru.kata.project.subscriptionDto.PaymentSessionResponseDto;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления запросами в API Gateway.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Контроллер API Gateway.", description = "Осуществляет взаимодействие между сервисами.")
public class GatewayController {
    private final ApiGatewayService apiGatewayService;
    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    @Autowired
    public GatewayController(ApiGatewayService apiGatewayService, KafkaProducerService kafkaProducerService,
                             WebClient.Builder webClientBuilder, KafkaConsumerService kafkaConsumerService) {
        this.apiGatewayService = apiGatewayService;
        this.kafkaProducerService = kafkaProducerService;
        this.kafkaConsumerService = kafkaConsumerService;
    }

    /**
     * Обработчик GET-запроса.
     *
     * @return строка "OK", указывающая, что шлюз работает корректно.
     */
    @GetMapping("/status")
    public String status() {
        return "OK";
    }

    /**
     * Эндпоинт для отправки запроса на оценку резюме.
     * Принимает resumeId и отправляет сообщение в Kafka.
     */
    @PostMapping("ai/resume-score")
    public ResponseEntity<Void> requestResumeScore(@RequestParam String resumeId) {
        if (resumeId == null) {
            return ResponseEntity.badRequest().build();
        } else {
            kafkaProducerService.sendScoreRequest(resumeId);
            return ResponseEntity.accepted().build();
        }
    }

    /**
     * Эндпоинт для получения результата оценки резюме.
     * Клиент опрашивает статус или получает готовый результат.
     */
    @GetMapping("/ai/resume-score")
    public ResponseEntity<ResumeScoreResult> getResumeScore(@RequestParam String resumeId) {
        final ResumeScoreResult result = kafkaConsumerService.getScoreResult(resumeId);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Добавляет маршрут для создания нового резюме.
     * Этот метод будет обрабатывать POST-запросы для создания резюме.
     *
     * @param resumeDto Данные для создания резюме, включая личные данные кандидата, образование и опыт.
     * @return Mono<ResponseEntity> с результатом обработки запроса.
     */
    @PostMapping("/resume/create")
    @Operation(summary = "Отправка JSON данных для создания резюме")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Успешная отправка"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    public Mono<ResponseEntity<JsonNode>> createResume(@RequestBody ResumeDto resumeDto) {
        return apiGatewayService.createResume(resumeDto);
    }

    /**
     * POST запрос на отправку AnalysisRequest в kafka топик RESUME_ANALYZE_REQUESTS,
     * для последующего анализа резюме сервисом Resume-Analyzer
     *
     * @param analysisRequest DTO для передачи данных между API Gateway и Resume-Analyzer
     * @return ResponseEntity с результатом обработки запроса.
     */
    @PostMapping("/analyze")
    @Operation(summary = "Отправка AnalysisRequest в kafka топик RESUME_ANALYZE_REQUESTS")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Успешная отправка"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    public ResponseEntity<AnalysisRequest> sendAnalysisRequestToKafka(@RequestBody AnalysisRequest analysisRequest) {
        log.info("Получен запрос на отправку сообщения: {}, в кафка топик: {}", analysisRequest,
                KafkaTopics.RESUME_ANALYZE_REQUESTS);
        kafkaProducerService.sendAnalysisRequest(UUID.randomUUID().toString(), analysisRequest);

        return ResponseEntity.ok(analysisRequest);
    }

    /**
     * Эндпоинт для создания новой подписки через Gateway.
     * Проксирует запрос на создание подписки в микросервис подписок.
     *
     * @param request Объект CreateSubscriptionRequest, содержащий данные для создания подписки.
     * @return Mono с ResponseEntity, содержащим PaymentSessionResponseDto
     * в случае успеха или соответствующую ошибку.
     */
    @PostMapping("/subscriptions/create")
    @Operation(summary = "Создать новую подписку через Gateway",
            description = "Проксирует запрос на создание подписки в микросервис подписок.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Подписка создана",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentSessionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "503", description = "Сервис подписок или платежный шлюз недоступен"),
            @ApiResponse(responseCode = "504", description = "Таймаут сервиса подписок или платежного шлюза"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка шлюза")
    })
    public Mono<ResponseEntity<PaymentSessionResponseDto>> createSubscription(@RequestBody CreateSubscriptionRequest request) {
        log.info("Получен запрос на создание подписки для userId: {}, type: {} через API Gateway.", request.getUserId(), request.getType());
        return apiGatewayService.createSubscription(request);
    }

    /**
     * Эндпоинт для отмены активной подписки через Gateway.
     * Проксирует запрос на отмену подписки в микросервис подписок.
     *
     * @param userId UUID пользователя, чья подписка должна быть отменена.
     * @return Mono с ResponseEntity, содержащим строковое сообщение
     * в случае успеха или соответствующую ошибку.
     */
    @PostMapping("/subscriptions/cancel")
    @Operation(summary = "Отменить активную подписку через Gateway",
            description = "Проксирует запрос на отмену подписки в микросервис подписок.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подписка успешно отменена"),
            @ApiResponse(responseCode = "404", description = "Активная подписка не найдена"),
            @ApiResponse(responseCode = "503", description = "Сервис подписок или платежный шлюз недоступен"),
            @ApiResponse(responseCode = "504", description = "Таймаут сервиса подписок или платежного шлюза"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка шлюза")
    })
    public Mono<ResponseEntity<String>> cancelSubscription(@RequestParam UUID userId) {
        log.info("Получен запрос на отмену подписки для userId: {} через API Gateway.", userId);
        return apiGatewayService.cancelSubscription(userId);
    }

    /**
     * Эндпоинт для получения истории подписок пользователя через Gateway.
     * Проксирует запрос на получение истории подписок в микросервис подписок.
     *
     * @param userId UUID пользователя, чью историю подписок необходимо получить.
     * @return Mono с ResponseEntity, содержащим список SubscriptionHistoryDto в случае успеха
     * или соответствующую ошибку.
     */
    @GetMapping("/subscriptions/history")
    @Operation(summary = "Получить историю подписок пользователя через Gateway",
            description = "Проксирует запрос на получение истории подписок в микросервис подписок.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список записей истории подписок",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = SubscriptionHistoryDto.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка шлюза")
    })
    public Mono<ResponseEntity<List<SubscriptionHistoryDto>>> getSubscriptionHistory(@RequestParam UUID userId) {
        log.info("Получен запрос на получение истории подписок для userId: {} через API Gateway.", userId);
        return apiGatewayService.getSubscriptionHistory(userId);
    }

    /**
     * Эндпоинт для получения активной подписки пользователя через Gateway.
     * Проксирует запрос на получение активной подписки в микросервис подписок.
     *
     * @param userId UUID пользователя, чью активную подписку необходимо получить.
     * @return Mono с ResponseEntity, содержащим UserSubscriptionDto
     * в случае успеха или соответствующую ошибку (например, 404 Not Found).
     */
    @GetMapping("/subscriptions/active")
    @Operation(summary = "Получить активную подписку пользователя через Gateway",
            description = "Проксирует запрос на получение активной подписки в микросервис подписок.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Активная подписка найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserSubscriptionDto.class))),
            @ApiResponse(responseCode = "404", description = "Активная подписка не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка шлюза")
    })
    public Mono<ResponseEntity<UserSubscriptionDto>> getActiveSubscription(@RequestParam UUID userId) {
        log.info("Получен запрос на получение активной подписки для userId: {} через API Gateway.", userId);
        return apiGatewayService.getActiveSubscription(userId);
    }
}
