package ru.kata.project.webSocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.project.application.service.KafkaConsumerService;
import ru.kata.project.application.service.KafkaProducerService;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.ResumeScoreResult;
import ru.kata.project.webSocket.component.WebSocketSessionRegistry;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class WebSocketController {
    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;
    private final WebSocketSessionRegistry webSocketSessionRegistry;

    @Autowired
    public WebSocketController(KafkaProducerService kafkaProducerService, WebSocketSessionRegistry webSocketSessionRegistry, KafkaConsumerService kafkaConsumerService) {
        this.kafkaProducerService = kafkaProducerService;
        this.kafkaConsumerService = kafkaConsumerService;
        this.webSocketSessionRegistry = webSocketSessionRegistry;
    }

    @PostMapping("/analyze")
    @Operation(summary = "Отправка запроса на анализ резюме")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Запрос принят"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> sendAnalysisRequestToKafka(@RequestBody AnalysisRequest analysisRequest) {
        String requestId = UUID.randomUUID().toString();
        log.info("Сгенерирован requestId: {}, клиент должен установить WebSocket сессию и передать этот Id", requestId);
        analysisRequest.setRequestId(requestId);
        // Отправка запроса в Kafka
        kafkaProducerService.sendAnalysisRequest(requestId, analysisRequest);
        // Немедленно отправляем код 202 - "accept"
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/ai/resume-score")
    @Operation(summary = "Получение результата анализа резюме")
    public ResponseEntity<ResumeScoreResult> getResumeScore(@RequestParam String resumeId) {
        ResumeScoreResult result = kafkaConsumerService.getScoreResult(resumeId);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(result);
    }
}
