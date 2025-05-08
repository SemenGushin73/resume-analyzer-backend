package ru.javabegin.micro.planner.notificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import ru.javabegin.micro.planner.notificationservice.component.WebSocketSessionRegistry;

@Service
public class KafkaConsumerService {
    private final WebSocketSessionRegistry registry;

    @Autowired
    public KafkaConsumerService(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @KafkaListener(topics = "resume-analysis-result", groupId = "notification-group")
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
        return json.substring(json.indexOf(":\"") + 2, json.indexOf("\"}"));
    }
}
