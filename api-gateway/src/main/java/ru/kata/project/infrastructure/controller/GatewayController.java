package ru.kata.project.infrastructure.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
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
    public GatewayController(ApiGatewayService apiGatewayService,
                             KafkaProducerService kafkaProducerService,
                             WebClient.Builder webClientBuilder,
                             KafkaConsumerService kafkaConsumerService) {
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
     * @param resumeJson Данные для создания резюме, включая личные данные кандидата, образование и опыт.
     * @return ResponseEntity с результатом обработки запроса.
     */
    @PostMapping("/resume/create")
    public ResponseEntity<JsonNode> createResume(@RequestBody JsonNode resumeJson) {
        return apiGatewayService.createResume(resumeJson);
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная отправка"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<AnalysisRequest> sendAnalysisRequestToKafka(@RequestBody AnalysisRequest analysisRequest) {
        log.info("Получен запрос на отправку сообщения: {}, в кафка топик: {}",
                analysisRequest, KafkaTopics.RESUME_ANALYZE_REQUESTS);
        kafkaProducerService.sendAnalysisRequest(UUID.randomUUID().toString(),
                analysisRequest);

        return ResponseEntity.ok(analysisRequest);
    }
}
