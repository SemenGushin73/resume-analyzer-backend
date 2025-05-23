package ru.kata.project.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kata.project.application.service.KafkaProducerService;
import ru.kata.project.infrastructure.webSocket.NotificationWebSocketHandler;
import ru.kata.project.kafka.AnalysisRequest;

import java.util.UUID;

/**
 * Принимает HTTP POST-запрос с резюме.
 * Генерирует requestId (уникальный идентификатор запроса).
 * Отправляет запрос в Kafka.
 * Немедленно отвечает клиенту со статусом 202 Accepted.
 * Возвращает requestId в теле ответа и в заголовке X-Request-ID.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Контроллер WebSocket.",
        description = "Осуществление " +
                      "взаимодействия по протоколу webSocket.")
public class NotificationController {
    private final KafkaProducerService kafkaProducerService;

    @Operation(
            summary = "Отправка сообщения для анализа резюме",
            description = """
                    отправляем сообщение в топик RESUME_ANALYZE_RESULTS,
                    сразу же получаем ответ-код 202,
                    запоминаем X-Request-ID
                    """
    )
    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeResume(
            @RequestBody AnalysisRequest request) {
        final String requestId = UUID.randomUUID().toString();
        kafkaProducerService.sendAnalysisRequest(requestId, request);
        return ResponseEntity.accepted()
                .header("X-Request-ID", requestId)
                .body(requestId);
    }
}