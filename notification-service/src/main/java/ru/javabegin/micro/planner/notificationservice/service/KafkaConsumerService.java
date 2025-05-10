package ru.javabegin.micro.planner.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import reactor.core.publisher.Mono;
import ru.javabegin.micro.planner.notificationservice.component.WebSocketSessionRegistry;
import ru.javabegin.micro.planner.notificationservice.controller.ResumeAnalysisController;



@Service
public class KafkaConsumerService {
    private final WebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ResumeAnalysisController.class);

    @Autowired
    public KafkaConsumerService(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @KafkaListener(topics = "resume-analyze-results", groupId = "notification-group")
    public void listen(String message) {
        try {
            // Предположим, что message содержит requestId в формате: "{\"requestId\": \"...\", ...}"
            final String requestId = extractRequestIdFromMessage(message);
            final WebSocketSession session = registry.getSessionByRequestId(requestId);

            log.info("Received Kafka message with requestId={}, session={}", requestId, session);

            if (session != null && session.isOpen()) {
                WebSocketMessage wsMessage = session.textMessage(message);
                session.send(Mono.just(wsMessage))
                        .doOnSuccess(v -> log.info("WebSocket message sent for requestId={}", requestId))
                        .doOnError(e -> log.error("Failed to send WebSocket message", e))
                        .subscribe();
            } else {
                log.warn("No open WebSocket session for requestId={}", requestId);
            }
        } catch (Exception e) {
            log.error("Error while processing Kafka message", e);
        }
    }

    private String extractRequestIdFromMessage(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.get("requestId").asText();
        } catch (Exception e) {
            log.error("Failed to extract requestId from message: {}", json, e);
            return null;
        }
    }
}
