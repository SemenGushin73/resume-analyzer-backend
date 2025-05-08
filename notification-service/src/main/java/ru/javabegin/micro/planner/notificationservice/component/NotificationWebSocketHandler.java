package ru.javabegin.micro.planner.notificationservice.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Mono;

@Component
public class NotificationWebSocketHandler implements WebSocketHandler {
    private final WebSocketSessionRegistry registry;
@Autowired
    public NotificationWebSocketHandler(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(requestId -> {
                    registry.registerSession(session, requestId);
                    return Mono.empty();
                })
                .then();
    }
}
