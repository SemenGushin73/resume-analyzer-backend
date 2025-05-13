package ru.kata.project.application.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;
import ru.kata.project.component.WebSocketSessionRegistry;
import ru.kata.project.kafka.AnalysisRequest;
import ru.kata.project.kafka.AnalysisResult;
import ru.kata.project.kafka.config.KafkaTopics;

@Service
public class KafkaConsumerService {
    private final WebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    public KafkaConsumerService(WebSocketSessionRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.RESUME_ANALYZE_RESULTS, groupId = "notification-group")
    public void listen(@Header(KafkaHeaders.RECEIVED_KEY) String requestId, @Payload String jsonPayload) {
        try {
            log.info("Received Kafka message with requestId={}", requestId);
            //Парсим JSON в DTO
            AnalysisResult result = objectMapper.readValue(jsonPayload, AnalysisResult.class);
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
            String response = "Result: " + result.getResult();  // Пример
            session.send(Mono.just(session.textMessage(response))).subscribe();

        } catch (
                Exception e) {
            log.error("Error while processing Kafka message", e);
        }
    }
}
