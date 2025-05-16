package ru.kata.project.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import ru.kata.project.component.WebSocketSessionRegistry;
import ru.kata.project.kafka.AnalysisResult;
import ru.kata.project.kafka.config.KafkaTopics;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс является потребителем (consumer) сообщений из Kafka, который:
 * Получает результаты анализа резюме (из топика RESUME_ANALYZE_RESULTS).
 * Отправляет эти результаты через WebSocket соответствующему клиенту (фронтенду).
 */
@Service
public class KafkaConsumerService {
    private final WebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper;
    private final Map<String, AnalysisResult> results = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    public KafkaConsumerService(WebSocketSessionRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.RESUME_ANALYZE_RESULTS, groupId = "notification-group")
    public void consume(String requestId, AnalysisResult result) {
        log.info("Processing message from topic: {}. Request ID: {}, Data: {}",
                KafkaTopics.RESUME_ANALYZE_RESULTS, result.getRequestId(), result);
        try {
            if (requestId == null) {
                requestId = result.getRequestId();  // Попробуем взять из тела
            }
            results.put(requestId, result);
            log.info("Message processed successfully. Topic: {}, RequestId: {}",
                    KafkaTopics.RESUME_ANALYZE_RESULTS, requestId);
            final WebSocketSession session = registry.getSessionByRequestId(requestId);
            if (session == null) {
                log.warn("No session found for requestId={}", requestId);
                return;
            }
            if (!session.isOpen()) {
                log.warn("Session is closed for requestId={}", requestId);
                registry.removeSession(requestId);
                return;
            }
            log.info("Session from requestId={}, with sessionId ={}",
                    requestId, registry.getSessionByRequestId(requestId));
            // Формируем структурированный JSON ответ
            final String response = objectMapper.writeValueAsString(Map.of(
                    "type", "analysisResult",
                    "requestId", requestId,
                    "data", result
            ));
            session.send(Mono.just(session.textMessage(response))).subscribe();
        } catch (
                Exception e) {
            log.error("Error while processing Kafka message", e);
        }
    }
}
