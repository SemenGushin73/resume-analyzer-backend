package ru.kata.project.infrastructure.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import ru.kata.project.application.service.KafkaProducerService;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.config.KafkaTopics;
import java.util.UUID;

/**
 * Клас реализует приём запросов от клиента (Swagger UI или фронтенд).
 * Асинхронно отправляет эти запросы в Kafka для дальнейшей обработки.
 * Немедленно возвращает клиенту 202 Accepted о том что запрос принят в систему.
 */
@RestController
@RequestMapping("/analyze")
@Tag(name = "Контроллер клиента.", description = "Осуществляет HTTP-запрос через Swagger или Фронтенд")
public class ResumeAnalysisController {
    private final KafkaTemplate<String, AnalysisRequest> kafkaTemplate;
    private final KafkaProducerService kafkaProducerService;
    private static final Logger log = LoggerFactory.getLogger(ResumeAnalysisController.class);

    @Autowired
    public ResumeAnalysisController(KafkaTemplate<String, AnalysisRequest> kafkaTemplate, KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Operation(summary = "Отправка http запроса через Swagger", description = """
            Запрос пробрасывается в Kafka (produce).
            Немедленно возвращается 202 Accepted.
            """)
    @ApiResponse(responseCode = "202", description = "Запрос ушел в Kafka", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AnalysisRequest.class)))
    @CrossOrigin(origins = "http://localhost:8885")
    @PostMapping()
    public ResponseEntity<Void> analyzeResume(@RequestBody AnalysisRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("Получен запрос на отправку сообщения: {}, в кафка топик: {}", request, KafkaTopics.RESUME_ANALYZE_REQUESTS);
        kafkaTemplate.send(KafkaTopics.RESUME_ANALYZE_REQUESTS, requestId, request);
        // Немедленно возвращаем 202 Accepted и requestId
        return ResponseEntity.accepted().header("X-Request-ID", requestId).build();
    }

    @Operation(summary = "Отправка AnalysisRequest в kafka топик RESUME_ANALYZE_REQUESTS")
    @ApiResponse(responseCode = "202", description = "Запрос ушел в Kafka", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AnalysisRequest.class)))

    @PostMapping("/analyzeRequest")
    public ResponseEntity<AnalysisRequest> sendAnalysisRequestToKafka(@RequestBody AnalysisRequest request) {
        log.info("Получен запрос на отправку сообщения: {}, в кафка топик: {}, ",
                request, KafkaTopics.RESUME_ANALYZE_REQUESTS);
        String requestId = UUID.randomUUID().toString();
        kafkaProducerService.sendAnalysisRequest(requestId,
                request);
        return ResponseEntity.accepted().header("X-Request-ID", requestId).build();
    }
}
