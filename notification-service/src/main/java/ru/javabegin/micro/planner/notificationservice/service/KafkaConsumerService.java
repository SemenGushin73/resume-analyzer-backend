package ru.javabegin.micro.planner.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import ru.javabegin.micro.planner.notificationservice.component.WebSocketSessionRegistry;
@Slf4j
@Service
public class KafkaConsumerService {
    private final WebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public KafkaConsumerService(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @KafkaListener(topics = "KafkaTopics.RESUME_ANALYZE_REQUESTS", groupId = "notification-group")
    public void listen(String message) {
        // Предположим, что message содержит requestId в формате: "{\"requestId\": \"...\", ...}"
        final String requestId = extractRequestIdFromMessage(message);
        final WebSocketSession session = registry.getSessionByRequestId(requestId);
        if (session != null && session.isOpen()) {
            final WebSocketMessage wsMessage = session.textMessage(message);
            Mono.from(session.send(Mono.just(wsMessage))).subscribe();
        }
    }

    private String extractRequestIdFromMessage(String json) {
        // Использовать JsonPath или любой простой способ для вытаскивания requestId
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.get("requestId").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
