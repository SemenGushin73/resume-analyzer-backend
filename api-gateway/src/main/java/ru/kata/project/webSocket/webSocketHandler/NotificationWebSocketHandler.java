package ru.kata.project.webSocket.webSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import ru.kata.project.webSocket.component.WebSocketSessionRegistry;
@Component
public class NotificationWebSocketHandler implements WebSocketHandler {
    private final WebSocketSessionRegistry sessionRegistry;
@Autowired
    public NotificationWebSocketHandler(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(payload -> {
                    String requeatId = extractRequestId(payload);
                    sessionRegistry.registerSession(requeatId, session);
                    return Mono.empty();
                })
                .then();
    }

    private String extractRequestId(String payload) {
        try {
            return new ObjectMapper().readTree(payload).get("requestId").asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid request ID: " + e);
        }
    }
}
