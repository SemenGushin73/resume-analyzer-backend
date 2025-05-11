package ru.javabegin.micro.planner.notificationservice.service;

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
import ru.javabegin.micro.planner.notificationservice.component.WebSocketSessionRegistry;
import ru.kata.project.kafka.config.KafkaTopics;

@Service
public class KafkaConsumerService {
    private final WebSocketSessionRegistry registry;
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    public KafkaConsumerService(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @KafkaListener(topics = KafkaTopics.RESUME_ANALYZE_RESULTS, groupId = "notification-group")
    public void listen(@Header(KafkaHeaders.RECEIVED_KEY) String requestId, @Payload String message) {
        try {
            // Предположим, что message содержит requestId в формате: "{\"requestId\": \"...\", ...}"
            final WebSocketSession session = registry.getSessionByRequestId(requestId);
            log.info("Received Kafka message with requestId={}, session={}", requestId, session);
            log.info("Session isOpen for requestId={}: {}", requestId, session != null && session.isOpen());
            if (session != null && session.isOpen()) {
                // Предположим, что анализируем результат, например, строим сообщение для отправки через WebSocket
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
}
